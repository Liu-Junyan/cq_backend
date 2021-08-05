package server

import Session
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer

private val logger = KotlinLogging.logger {}

class ServerRunner: Thread() {
    override fun run() {
        val app: HttpHandler = routes(
            "/" bind Method.POST to { req: Request ->
                processRequest(req)
                Response(OK)
            }
        )
        app.asServer(ApacheServer(5701)).start()
    }
}

fun processRequest(req: Request) {
    val request = req.body.stream.bufferedReader().readLine()
    val message = Json { ignoreUnknownKeys = true }.decodeFromString<Message>(request)
    logger.info { message }
    val session = getSessionFromMessage(message)
    if (session != null) {
        val matchIndex = Regex("""^\.[0-9]+""").find(message.raw_message)
        if (matchIndex != null) {
            val respond = responseToIndexCommand(matchIndex.value, session)
            session.sendMsg(respond)
            return
        }
        val match = Regex("""^\.[A-Za-z]+""").find(message.raw_message)
        if (match != null) { // matched
            val respond = responseToWordCommand(match.value.lowercase(), message.raw_message, session)
            session.sendMsg(respond)
        }
    } else {
        throw Exception("Recipient not registered.")
    }
}

fun getSessionFromMessage(message: Message): Session? {
    val session = if (message.message_type == "private") {
        SessionPool.getSession(message.user_id, RecipientType.Individual)
    } else {
        SessionPool.getSession(message.group_id!!, RecipientType.Group)
    }
    return session
}

fun responseToWordCommand(command: String, full_message: String, session: Session): String = when(command) {
    ".help" -> """
        .help -> help page
        .live -> get current live status
        .[index] -> get detailed info of a live stream
    """.trimIndent()
    ".live" -> session.liveMsg()
    else -> "No such command."
}

fun responseToIndexCommand(command: String, session: Session): String {
    val index = command.substring(1).toInt()
    val live = session.getNthLive(index)
    return live?.getInfo() ?: "Index out of scoped."
}