import kotlinx.serialization.Serializable

@Serializable
data class Recipients(val groups: List<Recipient>, val individuals: List<Recipient>)

@Serializable
data class Recipient(val id: Long, val frequency: Int)