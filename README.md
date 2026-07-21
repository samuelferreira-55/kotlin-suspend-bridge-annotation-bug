# kotlin-suspend-bridge-annotation-bug

Minimal reproducer for a Kotlin 2.4.0 regression that breaks Quarkus REST endpoint scanning.

## The bug

A JAX-RS resource that **overrides** a generic `suspend fun` inherited from an abstract base class
fails Quarkus's build-time endpoint processing with:

```
java.lang.IllegalArgumentException: Continuation parameter type not parameterized: kotlin.coroutines.Continuation
	at io.quarkus.arc.processor.KotlinUtils.getKotlinSuspendMethodResult(KotlinUtils.java:52)
	at io.quarkus.resteasy.reactive.server.deployment.ResteasyReactiveProcessor$4.apply(...)
```

The entire reproducer is one file, [`src/main/kotlin/Bug.kt`](src/main/kotlin/Bug.kt):

```kotlin
abstract class Base<T : Any> {
    open suspend fun update(dto: T): T = throw UnsupportedOperationException()
}

@Path("/foo")
@ApplicationScoped
class FooResource : Base<String>() {

    @PUT
    override suspend fun update(dto: String): String = dto
}
```

Just **inheriting** `update()` without overriding it never triggers the bug. It's specifically an
`override` that redeclares a generic method's signature with the type parameter already resolved
to a concrete type (`String` instead of `T`) that breaks.

### Versions tested

| Kotlin | Quarkus | REST extension | Result |
|---|---|---|---|
| 2.3.21 | 3.37.3 | `quarkus-rest-jackson` | ✅ BUILD SUCCESSFUL |
| **2.4.0** | 3.37.3 | `quarkus-rest-jackson` | ❌ fails (see above) |
| 2.4.0 | 3.35.2 | `quarkus-rest-jackson` | ❌ fails (same error) |
| 2.4.0 | 3.37.3 | `quarkus-resteasy-reactive-jackson:3.15.7` (legacy) | ❌ fails (same error) |

Kotlin 2.3.21 and 2.4.0 are consecutive releases (no intermediate version exists on Maven Central),
so the regression was introduced somewhere in that release. The Quarkus version and which REST
extension (legacy RESTEasy Reactive vs. current Quarkus REST) is used make no difference — only the
Kotlin compiler version does.

### Reproduce it

```shell script
./gradlew build
```

Change `kotlinVersion` in `gradle.properties` to `2.3.21` to see it pass.

---

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/kotlin-suspend-bridge-annotation-bug-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
