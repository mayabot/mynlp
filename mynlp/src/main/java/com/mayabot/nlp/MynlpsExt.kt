package com.mayabot.nlp.kotlin

import com.mayabot.nlp.Mynlp

inline fun <reified T> Mynlp.getInstance(): T {
    return this.getInstance(T::class.java)
}