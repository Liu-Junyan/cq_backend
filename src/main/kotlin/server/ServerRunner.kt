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

private val logger = KotlinLogging.logger {}

val app: HttpHandler = routes(
    "/" bind Method.POST to { req: Request ->
        processRequest(req)
        Response(OK)
    }
)

fun processRequest(req: Request) {
    val request = req.body.stream.bufferedReader().readLine()
    val message = Json { ignoreUnknownKeys = true }.decodeFromString<Message>(request)
    logger.debug { message }
    val session = getSessionFromMessage(message)
    if (session != null) {
        val matchIndex = Regex("""^\.[0-9]+""").find(message.raw_message)
        if (matchIndex != null) {
            val command = matchIndex.value
            val respond = responseToIndexCommand(command, session)
            session.sendMsg(respond, command)
            return
        }
        val match = Regex("""^\.[A-Za-z]+""").find(message.raw_message)
        if (match != null) { // matched
            val command = match.value.lowercase()
            val respond = responseToWordCommand(command, session)
            session.sendMsg(respond, command)
        }
    } else {
        throw Exception("Recipient not registered.")
    }
}

fun getSessionFromMessage(message: Message): Session? = when(message.message_type) {
    "private" -> SessionPool.getSession(message.user_id, RecipientType.Individual)
    "group" -> SessionPool.getSession(message.group_id!!, RecipientType.Group)
    else -> null
}

fun responseToWordCommand(command: String, session: Session): String = when(command) {
    ".help" -> """
        .help -> help page
        .live -> get current live status
        .[index] -> get detailed info of a live stream, e.g., .1
    """.trimIndent()
    ".live" -> session.liveMsg()
    else -> "No such command."
}

fun responseToIndexCommand(command: String, session: Session): String {
    val index = command.substring(1).toInt()
    val live = session.getNthLive(index)
    return live?.getInfo() ?: "Index out of scoped."
}