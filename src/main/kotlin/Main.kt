import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path

abstract class Base<T : Any> {
    open suspend fun update(dto: T): T = throw UnsupportedOperationException()
}

@Path("/foo")
@ApplicationScoped
class FooResource : Base<String>() {

    @PUT
    override suspend fun update(dto: String): String = dto
}