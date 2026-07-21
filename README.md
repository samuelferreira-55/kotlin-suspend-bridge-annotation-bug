# PR with fix in Quarkus: https://github.com/quarkusio/quarkus/pull/55579



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

### Reproduce it

```shell script
./gradlew build
```
