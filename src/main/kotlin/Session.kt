import mu.KotlinLogging
import java.net.URL
import java.net.URLEncoder
import kotlin.properties.Delegates

private val logger = KotlinLogging.logger {}

class Session(val recipient: Recipient) {
    private var liveList: MutableList<Live> by Delegates.observable(mutableListOf()) { _, oldValue, newValue ->
        logger.debug { "LiveList at ${recipient.type.name} ${recipient.id} changed." }
        logger.debug { "Old value is $oldValue" }
        logger.debug { "New value is $newValue" }
    }

    init {
        liveList = LiveList.getGlobalLiveList().map { it.copy() }.toMutableList()
    }

    fun getNthLive(n: Int): Live? {
        logger.debug { liveList }
        return if (n <= liveList.size) liveList[n - 1] else null
    }

    fun updateAndSendLive() {
        val msg = liveMsg()
        sendMsg(msg)
    }

    fun shouldSendAt(min: Int): Boolean {
        val element = 60 / recipient.frequency
        return min % element == 0
    }

    fun liveMsg(): String {
        liveList = LiveList.getGlobalLiveList().map { it.copy() }.toMutableList()
        var msg = "Live now:"
        if (liveList.isEmpty()) {
            msg = "NaN"
        } else {
            var index = 1
            for (live in liveList){
                msg += "\n$index. ${live.channel.name}: ${live.title}"
                index++
            }
        }
        return msg
    }

    fun sendMsg(msg: String, command: String? = null) {
        var msgModified = if (command != null) "$command ->\n$msg" else msg
        msgModified = URLEncoder.encode(msgModified, "utf-8")
        val url = if (recipient.type == RecipientType.Group) {
            "http://127.0.0.1:5700/send_msg?group_id=${recipient.id}&message=$msgModified"
        } else {
            "http://127.0.0.1:5700/send_msg?user_id=${recipient.id}&message=$msgModified"
        }
        URL(url).readText()
        logger.info { "Sent message to ${recipient.type.name} ${recipient.id}." }
    }
}