fun main(args: Array<String>) {
    val text = "你好"

    text.toCharArray().forEach {
        println(it.toChar())
    }

    val list = ArrayList<Char>(10)
    list.add('a')
}