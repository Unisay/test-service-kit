Test Service Kit
================

Scala framework that manages external services for tests (mock HTTP services, docker containers, databases, etc.)

## Usage
### Add the test-service-kit dependency to your project

```scala
resolvers += "Zalando Releases" at "https://maven.zalando.net/content/groups/public/content/repositories/releases"

libraryDependencies += "org.zalando" %% "test-service-kit" % "0.2"
```

### Implement application-specific test services by extending base services:

Currently there are 3 base services available:

#### MockServer (www.mock-server.com)
... represents Mock-Server instance.

```scala
class MyOauthTestService extends MockServerTestService {
   // implement required methods here
}
```
See [MockServerTestService](/src/main/scala/org/zalando/test/kit/service/MockServerTestService.scala) 

#### Docker Container
... represents Docker container.

```scala
class MyDockerContainerTestService extends DockerTestService {
   // implement required methods here
}
```
See [DockerTestService](/src/main/scala/org/zalando/test/kit/service/DockerTestService.scala)

#### DatabaseTestService (Embedded PostgreSQL)
See [DatabaseTestService](/src/main/scala/org/zalando/test/kit/service/DatabaseTestService.scala)

### Mixin trait
Add ScalatestServiceKit trait to your tests:

For ScalaTest:
```scala
case MyCoolSpec extends FlatSpec with ScalatestServiceKit { ... }
```

For specs2: 
```
Not implemented yet, pull requests are welcome.
```

### Define services used by test
```scala
case MyCoolSpec extends FlatSpec with ScalatestServiceKit {
  val databaseTestService = new DatabaseTestService(databaseConfig) // May be used directly, without extending it
  val oauthTestService = new MyOauthTestService(webServiceConfig) // Specific to your application
  val dockerContainer = new MyDockerContainerTestService(dockerContainerConfig) // Specific to your application
  override def testServices: List[TestService] = List(databaseTestService, oauthTestService)
}

```

After that lifecycle of each test service will be attached to the test lifecycle: they are started/reset/stopped by your testing framework (Scalatest, Specs2, ...). 

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
