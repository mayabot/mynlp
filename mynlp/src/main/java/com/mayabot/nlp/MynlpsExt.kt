package com.mayabot.nlp

inline fun <reified T> Mynlp.getInstance(): T {
    return this.getInstance(T::class.java)
}