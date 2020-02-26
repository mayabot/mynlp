package com.mayabot.nlp.common

import java.util.ArrayList


fun checkNotNull(obj: Any?) {
    kotlin.checkNotNull(obj)
}


object Lists{

    @JvmStatic
    fun <T> newArrayList() : ArrayList<T>{
        return ArrayList()
    }
}

object Maps{

    @JvmStatic
    fun <A,B> newHashMap() : HashMap<A,B>{
        return java.util.HashMap<A,B>()
    }

    @JvmStatic
    fun <A,B> newHashMap(from:Map<A,B>) : HashMap<A,B>{
        return java.util.HashMap<A,B>(from)
    }

}