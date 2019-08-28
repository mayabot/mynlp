import com.mayabot.mynlp.restful.module
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty


fun main() {

    val server = embeddedServer(Netty, 8080) {
        module()
    }

    server.start(wait = true)
}