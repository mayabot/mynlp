package com.mayabot.nlp.fasttext.utils

private var doLog = true

fun disableLog() {
    doLog = false
}

fun enableLog() {
    doLog = true
}

fun logger(s: Any) {
    if (doLog) print(s)
}

fun loggerln(s: Any) {
    if (doLog) println(s)
}

fun loggerln() {
    if (doLog) println()
}