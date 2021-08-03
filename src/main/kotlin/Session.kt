import kotlinx.serialization.json.*
import mu.KotlinLogging
import java.net.URL
import java.net.URLEncoder

private val logger = KotlinLogging.logger {}

class Session(val recipient: Recipient, val recipientType: RecipientType, val min: Int, var msg: String = "LIVE NOW:") {
    init {
        logger.info { "${recipient.id}: ${recipient.frequency}, $recipientType" }
        if (!(recipient.frequency == 1 && min == 32)){ // Skip this situation
            val response = getResponse()
            val liveObjList = parseJson(response)
            constructMsg(liveObjList)
            sendMsg()
        }
    }

    private fun getResponse(): String {
        val url = "https://api.holotools.app/v1/live?hide_channel_desc=1&lookback_hours=0"
        return URL(url).readText()
    }
    private fun parseJson(text: String): MutableList<Live> {
        val obj = Json.parseToJsonElement(text)
        val liveList = obj.jsonObject["live"]!!.jsonArray
        val res = mutableListOf<Live>()
        for (live in liveList) {
            if (live.jsonObject["channel"]!!.jsonObject["subscriber_count"]!!.jsonPrimitive.int > 500000) {
                val currLive = Live(live.jsonObject["channel"]!!.jsonObject["name"]!!.jsonPrimitive.content, live.jsonObject["title"]!!.jsonPrimitive.content)
                res.add(currLive)
            }
        }
        res.sortBy { it.channel }
        return res
    }
    private fun constructMsg(liveObjList: MutableList<Live>){
        for (live in liveObjList){
            msg += "\n${live.channel}: ${live.title}"
        }
        msg = URLEncoder.encode(msg, "utf-8")
    }
    private fun sendMsg(){
        if (recipientType == RecipientType.GROUP) {
            val url = "http://127.0.0.1:5700/send_msg?group_id=${recipient.id}&message=$msg"
            val response = URL(url).readText()
            logger.info {"Sent to group ${recipient.id}, result is $response"}
        } else if (recipientType == RecipientType.INDIVIDUAL) {
            val url = "http://127.0.0.1:5700/send_msg?user_id=${recipient.id}&message=$msg"
            val response = URL(url).readText()
            logger.info { "Sent to individual ${recipient.id}, result is $response" }
        }
    }
}