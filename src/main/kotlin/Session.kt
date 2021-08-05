import mu.KotlinLogging
import java.net.URL
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

class Session(val recipient: Recipient) {
    private var liveList: MutableList<Live> = mutableListOf()

    init {
        sendMsg("""Bot started. Use ".help" for guide.""")
    }

    fun getNthLive(n: Int): Live? {
        return if (n <= liveList.size) liveList[n - 1] else null
    }

    fun periodicUpdateAndSend() {
        liveList = LiveList.get()
        val msg = liveMsg()
        sendMsg(msg)
    }

    fun shouldSendAt(min: Int): Boolean {
        val element = 60 / recipient.frequency
        return min % element == 0
    }

    fun liveMsg(): String{
        var msg = "LIVE NOW:"
        if (liveList.isEmpty()) {
            msg = "NaN"
        }
        var index = 1
        for (live in liveList){
            msg += "\n$index. ${live.channel.name}: ${live.title}"
            index++
        }
        return msg
    }

    fun sendMsg(msg: String){
        val msgEncoded = URLEncoder.encode(msg, "utf-8")
        val url = if (recipient.type == RecipientType.Group) {
            "http://127.0.0.1:5700/send_msg?group_id=${recipient.id}&message=$msgEncoded"
        } else {
            "http://127.0.0.1:5700/send_msg?user_id=${recipient.id}&message=$msgEncoded"
        }
        val response = URL(url).readText()
        logger.info { "Sent to ${recipient.type.name} ${recipient.id}, result is $response" }
    }
}