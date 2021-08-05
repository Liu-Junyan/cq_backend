import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import server.ServerRunner
import java.io.IOException
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class Runner: CliktCommand() {
    private val configRecipients by option("-r", "--recipients", help = "Recipients Config path").file()
    private val debugMode by option("-d", "--debug", help = "Debug Mode").flag()

    override fun run() {
        // Reading recipients from config
        val configRecipientsText = configRecipients?.readText() ?: throw IOException("Recipient config file does not exist.")
        val recipientsConfig = Yaml.default.decodeFromString<RecipientsConfig>(configRecipientsText)
        LiveList.activateDebugMode(debugMode)
        SessionPool.load(recipientsConfig)

        ServerRunner().start()

        runBlocking {
            while (true) {
                val currentTime = LocalDateTime.now()
                val min = if (debugMode) currentTime.second else currentTime.minute
                logger.debug { min }
                launch {
                    SessionPool.periodicFire(min)
                }
                delay(if (debugMode) 1_000 else 60_000)
            }
        }
    }
}