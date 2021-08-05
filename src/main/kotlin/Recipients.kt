import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RecipientType {
    Group, Individual
}

@Serializable
data class RecipientsConfig(val recipients: List<Recipient>)

@Serializable
@SerialName("recipient")
data class Recipient(val id: Long, val type: RecipientType, val frequency: Int)

