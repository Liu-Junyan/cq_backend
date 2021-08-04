import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveJSONList(val live: List<Live>)

@Serializable
data class Live(val channel: Channel, var title: String) {
    init {
        title = removeBackBracket(title)
    }
    private fun removeBackBracket(title: String): String{
        val regex = Regex("【[^【]+?】$")
        return title.replace(regex, "")
    }
}

@Serializable
@SerialName("channel")
data class Channel(val name: String, val subscriber_count: Long)