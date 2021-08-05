object SessionPool {
    private val sessions: MutableSet<Session> = mutableSetOf<Session>()

    fun load(recipientsConfig: RecipientsConfig) {
        for (recipient in recipientsConfig.recipients) {
            sessions.add(Session(recipient))
        }
    }

    fun periodicFire(min: Int) {
        for (session in sessions) {
            if (session.shouldSendAt(min)) {
                session.periodicUpdateAndSend()
            }
        }
    }

    fun getSession(id: Long, type: RecipientType): Session? {
        return(sessions.find { it.recipient.id == id && it.recipient.type == type })
    }
}