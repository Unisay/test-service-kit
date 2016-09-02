Test Service Kit
================

[![Build Status](https://travis-ci.org/zalando-incubator/test-service-kit.svg?branch=master)](https://travis-ci.org/zalando-incubator/test-service-kit)
[![Join the chat at https://gitter.im/zalando/test-service-kit](https://badges.gitter.im/zalando/test-service-kit.svg)](https://gitter.im/zalando/test-service-kit?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Scala framework that manages external services for tests (mock HTTP services, docker containers, databases, etc.)

Central concept of the framework is a [TestService](/src/main/scala/org/zalando/test/kit/service/TestService.scala):

```scala
trait TestService {
  def name: String
  def beforeSuite(): Unit = {}
  def beforeTest(): Unit = {}
  def afterTest(): Unit = {}
  def afterSuite(): Unit = {}
}
```

It represents some process (OS process, JVM thread) that is run concurrently and separately from the test [suite] and provides useful function for it (inspired by [JUnits Rules] (https://github.com/junit-team/junit/wiki/Rules#externalresource-rules)) Some examples are: database, mock rest service, docker container, etc. 

Test Service Kit manages lifecycle of `TestService`s by calling following lifecycle methods:
```scala
def beforeSuite(): Unit
def beforeTest(): Unit
def afterTest(): Unit
def afterSuite(): Unit
```
Specific test service could be bound to test suite lifecycle only by mixing [SuiteLifecycle](/src/main/scala/org/zalando/test/kit/service/SuiteLifecycle.scala) trait, in this case it must implement 
```scala
def start(): Unit // called before suite
def stop(): Unit // called after suite
```

or to test lifecycle only by mixing [TestLifecycle](/src/main/scala/org/zalando/test/kit/service/SuiteLifecycle.scala) trait, in which case it must implement 
```scala
def start(): Unit // called before each test
def stop(): Unit // called after each test
```

## Installation
Add the test-service-kit dependency to your SBT project
```scala
libraryDependencies += "org.zalando" %% "test-service-kit" % "5.1.0"
```

## Usage

1. Implement your own test service by extending [TestService](/src/main/scala/org/zalando/test/kit/service/TestService.scala)
or use one of the already implemented test services:

  * [MockServerTestService](/src/main/scala/org/zalando/test/kit/service/MockServerTestService.scala) to run [MockServer](http://www.mock-server.com) instance.
    For example usage see [MockServerTestServiceSpec](/src/test/scala/org/zalando/test/kit/service/MockServerTestServiceSpec.scala)
  * [DockerContainerTestService](/src/main/scala/org/zalando/test/kit/service/DockerContainerTestService.scala) to run any [Docker](https://www.docker.com/) container.
    For example usage see [DockerContainerTestServiceSpec](/src/test/scala/org/zalando/test/kit/service/DockerContainerTestServiceSpec.scala)
  * [DatabaseTestService](/src/main/scala/org/zalando/test/kit/service/DatabaseTestService.scala) to run embedded PostgreSQL server.
    For example usage see [DatabaseTestServiceSpec](/src/test/scala/org/zalando/test/kit/service/DatabaseTestServiceDockerContainerTestServiceSpec.scala)
  * [JvmTestService](/src/main/scala/org/zalando/test/kit/service/JvmTestService.scala) to run JVM process.
    For example usage see [JvmTestServiceSpec](/src/test/scala/org/zalando/test/kit/service/JvmTestServiceSpec.scala)

2. Mixin trait to your spec
  * For [ScalaTest](http://scalatest.org/): [ScalatestServiceKit](/src/main/scala/org/zalando/test/kit/ScalatestServiceKit.scala)
  * For [Specs2](https://etorreborre.github.io/specs2/): [Specs2ServiceKit](/src/main/scala/org/zalando/test/kit/Specs2ServiceKit.scala)

3. Define services used by your spec:
  ```scala
  case MyCoolSpec extends FlatSpec with ScalatestServiceKit {
    val oauthApi = new MockServerTestService("Mocked REST API", port = 8080) with SuiteLifecycle
    val database = new DatabaseTestService("Embedded Postgres", port = 5432) with SuiteLifecycle
    val container = new DockerContainerTestService(config.get[DockerContainerConfig]("docker-container")) with TestLifecycle
    val app = new JvmTestService("My JVM App", mainClass = "org.zalando.test.kit.service.TestApplication") with SuiteLifecycle
  }
  ```
  
4. Define order in which test services are started/stopped:
  ```scala
  case MyCoolSpec extends FlatSpec with ScalatestServiceKit {
    ...
    override def testServices = (oauthApi || database) >> container
  }
  ```

  Legend:
  * `a || b` (alias `a inParallelWith b`) means test services `a` and `b` are started/stopped concurrently.
  * `a >> b` (alias `a andThen b`) means test services are started one after another (`a` then `b`) and stopped in reverse order (`b` then `a`).
  * For detailed example of composition see: [TestServiceCompositionSpec](/src/test/scala/org/zalando/test/kit/TestServiceCompositionSpec.scala)

## License

The MIT License (MIT)

Copyright (c) 2016 Zalando SE

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
