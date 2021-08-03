import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

class Runner: CliktCommand() {
    val configRecipients by option("-r", "--recipients", help = "Recipients Config path").file()

    override fun run() {
        // Reading recipients from config
        val recipients: Recipients = Json.decodeFromString<Recipients>(configRecipients!!.readText())

        runBlocking {
            while (true) {
                val currentTime = LocalDateTime.now()
                val min = currentTime.format(DateTimeFormatter.ofPattern("mm")).toInt() // Should be change to mm
                logger.info { min }
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
                delay(60_000) // Wait a minute, should be changed to 60_000
            }
        }
    }





}