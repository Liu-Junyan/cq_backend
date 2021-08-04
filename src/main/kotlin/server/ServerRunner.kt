package server

import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ServerRunner: Thread() {
    override fun run() {
        embeddedServer(Netty, port = 5701) {
            routing {
                getMsgRoute()
            }
        }.start(wait = true)
    }
}
