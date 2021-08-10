import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import server.app
import java.io.IOException
import java.time.LocalDateTime

class Runner: CliktCommand() {
    private val configRecipients by option("-r", "--recipients", help = "Recipients Config path").file()
    private val debugMode by option("-d", "--debug", help = "Debug Mode").flag()

    override fun run() {
        // Reading recipients from config
        val configRecipientsText = configRecipients?.readText() ?: throw IOException("Recipient config file does not exist.")
        val recipientsConfig = Yaml.default.decodeFromString<RecipientsConfig>(configRecipientsText)

        LiveList.init(debugMode)

        SessionPool.load(recipientsConfig)

        app.asServer(ApacheServer(5701)).start()

        Scheduler(interval = if (debugMode) 1_000 else 60_000, initialDelay = null, debugMode = debugMode).start()
    }
}