import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import server.ServerRunner
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class Runner: CliktCommand() {
    private val configRecipients by option("-r", "--recipients", help = "Recipients Config path").file()
    private val debugMode by option("-d", "--debug", help = "Debug Mode").flag()

    override fun run() {
        // Reading recipients from config
        val recipients: Recipients = Json.decodeFromString<Recipients>(configRecipients!!.readText())
        LiveList(debugMode)

//        ServerRunner().start()

        runBlocking {
            while (true) {
                val currentTime = LocalDateTime.now()
                val min = if (debugMode) currentTime.second else currentTime.minute
                logger.debug { min }
                if (min == 2 || min == 32) {
                    launch {
                        for (recipient in recipients.groups) {
                            Session(recipient, RecipientType.GROUP, min)
                        }
                        for (recipient in recipients.individuals) {
                            Session(recipient, RecipientType.INDIVIDUAL, min)
                        }
                    }
                }
                delay(if (debugMode) 1_000 else 60_000)
            }
        }
    }
}