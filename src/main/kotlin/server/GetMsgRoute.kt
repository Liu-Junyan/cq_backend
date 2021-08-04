package server

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.json.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun Route.getMsgRoute() {
    post("/") {
        logger.info { "Incoming request" }
        val text = call.receiveText()
        logger.info { text }
        val obj = Json.parseToJsonElement(text)
        val messageType =  obj.jsonObject["message_type"]?.jsonPrimitive?.content
        if (messageType != null && messageType == "private") {
            call.respondText("""{"reply": "ok"}""")
        }
    }
}