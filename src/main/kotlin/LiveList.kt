import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.net.URL
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

class LiveList(debugMode: Boolean) {
    init {
        LiveList.debugMode = debugMode
        update(LocalDateTime.now())
    }
    companion object {
        var liveList: MutableList<Live> = mutableListOf<Live>()
            get() {
                checkUpdate()
                return field
            }
        private lateinit var lastUpdateTime: LocalDateTime
        private var debugMode: Boolean = false

        private fun checkUpdate() {
            val initialTime = LocalDateTime.now()
            logger.debug { "Check for update at $initialTime" }
            val interval = if (debugMode) {
                ChronoUnit.SECONDS.between(lastUpdateTime, initialTime)
            } else {
                ChronoUnit.MINUTES.between(lastUpdateTime, initialTime)
            }
            if (interval >= 29) {
                update(initialTime)
            }
        }

        private fun update(initialTime: LocalDateTime) {
            lastUpdateTime = initialTime
            val response = getResponse()
            liveList.clear()
            parseJson(response)
            logger.info { "LiveList updated at $initialTime" }
        }
        private fun getResponse(): String {
            val url = "https://api.holotools.app/v1/live?hide_channel_desc=1&lookback_hours=0"
            return URL(url).readText()
        }
        private fun parseJson(text: String) {
            val liveJSONList = Json {ignoreUnknownKeys = true} .decodeFromString<LiveJSONList>(text)
            for (live in liveJSONList.live) {
                if (live.channel.subscriber_count > 50_0000) {
                    liveList.add(live)
                }
            }
            liveList.sortBy { it.channel.name }
        }
    }
}