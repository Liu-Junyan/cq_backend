import mu.KotlinLogging
import java.net.URL
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

class Session(val recipient: Recipient) {
    private var liveList: MutableList<Live> = mutableListOf()

    fun send() {
        liveList = LiveList.get()
        sendMsg(constructMsg(liveList))
    }

    fun shouldSendAt(min: Int): Boolean {
        val element = 60 / recipient.frequency
        return min % element == 0
    }

    private fun constructMsg(liveList: MutableList<Live>): String{
        var msg = "LIVE NOW:"
        if (liveList.isEmpty()) {
            msg = "NaN"
        }
        for (live in liveList){
            msg += "\n${live.channel.name}: ${live.title}"
        }
        msg = URLEncoder.encode(msg, "utf-8")
        return msg
    }
    private fun sendMsg(msg: String){
        val url = if (recipient.type == RecipientType.Group) {
            "http://127.0.0.1:5700/send_msg?group_id=${recipient.id}&message=$msg"
        } else {
            "http://127.0.0.1:5700/send_msg?user_id=${recipient.id}&message=$msg"
        }
        val response = URL(url).readText()
        logger.info { "Sent to ${recipient.type.name} ${recipient.id}, result is $response" }
    }
}