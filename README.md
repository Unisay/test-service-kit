Test Service Kit
================

Scala framework that manages external services for tests (mock HTTP services, docker containers, databases, etc.)

## Usage
### Add the test-service-kit dependency to your project

```scala
resolvers += "Zalando Releases" at "https://maven.zalando.net/content/groups/public/content/repositories/releases"

libraryDependencies += "org.zalando" %% "test-service-kit" % "0.1"
```
### Mixin trait
Add TestServiceKit trait to your tests:

For ScalaTest:
```scala
case MyCoolSpec extends FlatSpec with ScalaTestServiceKit { ... }
```

For specs2: TBD

### Define services used by test
```scala
case MyCoolSpec extends FlatSpec with ScalaTestServiceKit {
  val databaseTestService = new DatabaseTestService(databaseConfig) // Generic
  val oauthTestService = new OauthTestService(webServiceConfig) // Specific to your domain
  override def testServices: List[TestService] = List(databaseTestService, oauthTestService)
}

```

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