data class Live(val channel: String, var title: String) {
    init {
        title = removeBackBracket(title)
    }
    private fun removeBackBracket(title: String): String{
        val regex = Regex("【[^【]+?】$")
        return title.replace(regex, "")
    }
}