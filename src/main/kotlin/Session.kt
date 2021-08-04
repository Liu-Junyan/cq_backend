import mu.KotlinLogging
import java.net.URL
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

class Session(private val recipient: Recipient, private val recipientType: RecipientType, min: Int) {
    private var msg: String = "LIVE NOW:"
    init {
        logger.debug { "${recipient.id}: ${recipient.frequency}, $recipientType" }
        if (!(recipient.frequency == 1 && min == 32)){ // Skip this situation
            val liveList = LiveList.liveList
            constructMsg(liveList)
            sendMsg()
        }
    }

    private fun constructMsg(liveList: MutableList<Live>){
        if (liveList.isEmpty()) {
            msg = "NaN"
        }
        for (live in liveList){
            msg += "\n${live.channel.name}: ${live.title}"
        }
        msg = URLEncoder.encode(msg, "utf-8")
    }
    private fun sendMsg(){
        val url = if (recipientType == RecipientType.GROUP) {
            "http://127.0.0.1:5700/send_msg?group_id=${recipient.id}&message=$msg"
        } else {
            "http://127.0.0.1:5700/send_msg?user_id=${recipient.id}&message=$msg"
        }
        val response = URL(url).readText()
        logger.info { "Sent to ${recipientType.name} ${recipient.id}, result is $response" }
    }
}