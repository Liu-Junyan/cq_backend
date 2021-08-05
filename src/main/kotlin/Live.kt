import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveJSONList(val live: List<Live>)

@Serializable
data class Live(val channel: Channel, var title: String, val yt_video_key: String, val live_viewers: Int) {
    init {
        title = removeBackBracket(title)
    }
    private fun removeBackBracket(title: String): String{
        val regex = Regex("【[^【]+?】$")
        return title.replace(regex, "")
    }

    private fun getYoutubeURL(): String {
        return "http://youtu.be/$yt_video_key"
    }

    fun getInfo(): String {
        return """
            ${channel.name}
            $title
            ${getYoutubeURL()}
        """.trimIndent()
    }
}

@Serializable
@SerialName("channel")
data class Channel(val name: String, val subscriber_count: Long)