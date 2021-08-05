package server

import kotlinx.serialization.Serializable

@Serializable
data class Message(val raw_message: String, val message_type: String, val user_id: Long, val group_id: Long? = null)