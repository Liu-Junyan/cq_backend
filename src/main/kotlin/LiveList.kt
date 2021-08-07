import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.net.URL
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}


object LiveList {
    private var globalLiveList: MutableList<Live> = mutableListOf<Live>()
    private var lastUpdateTime: LocalDateTime? = null
    private var debugMode: Boolean = false

    fun init(debugMode: Boolean) {
        activateDebugMode(debugMode)
        checkUpdate()
    }

    fun getGlobalLiveList(): MutableList<Live> {
        checkUpdate()
        return globalLiveList
    }

    private fun activateDebugMode(debugMode: Boolean) {
        this.debugMode = debugMode
    }

    private fun checkUpdate() {
        val initialTime = LocalDateTime.now()
        if (lastUpdateTime == null) {
            update(initialTime)
        } else {
            logger.debug { "Check for update at $initialTime" }
            val interval = if (debugMode) {
                ChronoUnit.SECONDS.between(lastUpdateTime, initialTime)
            } else {
                ChronoUnit.MINUTES.between(lastUpdateTime, initialTime)
            }
            if (interval >= if (debugMode) 29 else 4) {
                update(initialTime)
            }
        }
    }

    private fun update(initialTime: LocalDateTime) {
        lastUpdateTime = initialTime
        val response = getResponse()
        globalLiveList.clear()
        liveListFromJSON(response)
        logger.info { "Updated at $initialTime" }
    }

    private fun getResponse(): String {
        val url = "https://api.holotools.app/v1/live?hide_channel_desc=1&lookback_hours=0"
        return URL(url).readText()
    }

    private fun liveListFromJSON(text: String) {
        val liveJSONList = Json {ignoreUnknownKeys = true} .decodeFromString<LiveJSONList>(text)
        globalLiveList = liveJSONList.live.filter { it.channel.subscriber_count >= 50_0000 }.toMutableList()
        globalLiveList.sortBy { it.channel.name }
    }
}
