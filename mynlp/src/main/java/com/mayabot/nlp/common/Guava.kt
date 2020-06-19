package com.mayabot.nlp.common

import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


fun checkNotNull(obj: Any?) {
    kotlin.checkNotNull(obj)
}

object Guava {
    @JvmStatic
    fun <T> join(iterable: Iterable<T>, sp: String): String {
        return iterable.joinToString(sp)
    }
//
//    fun trans(iterable: Iterable<WordTerm>): Iterable<String> {
//
//    }

    @JvmStatic
    fun split(text: String, sp: String): List<String> {
        return text.split(sp).map { it.trim() }.filter { it.isNotEmpty() }
    }

    @JvmStatic
    fun split(text: String, sp: Pattern): List<String> {
        return text.split(sp).map { it.trim() }.filter { it.isNotEmpty() }
    }

    @JvmStatic
    fun openBufIns(file: File) = file.inputStream().buffered()

    @JvmStatic
    fun openResource(name: String) = Guava::class.java.classLoader.getResourceAsStream(name)


    /**
     * Given a `resourceName` that is relative to `contextClass`,
     * returns a `URL` pointing to the named resource.
     *
     * @throws IllegalArgumentException if the resource is not found
     */
    @JvmStatic
    fun getResource(contextClass: Class<*>, resourceName: String?): URL? {
        val url = contextClass.getResource(resourceName)
        return url
    }

    @JvmStatic
    public fun <T> mutiadd(map: MutableMap<String, MutableList<T>>, key: String, value: T) {
        if (!map.containsKey(key)) {
            map.put(key, ArrayList<T>())
        }
        map[key]!!.add(value)
    }

    @JvmStatic
    fun readLines(url: URL): List<String> {
        return url.readText(Charsets.UTF_8).lines()
    }

    @JvmStatic
    fun <T> concatIterables(vararg iter: Iterable<T>): Iterable<T> {
        return sequenceOf(*iter).flatten().asIterable()
    }

    @JvmStatic
    fun readLines(file: File, charset: Charset): List<String> {
        return file.readLines(charset)
    }

    @JvmStatic
    fun checkNotNull(obj: Any?): Any {
        return obj!!
    }

}

object Lists {

    @JvmStatic
    fun <T> newArrayList(): ArrayList<T> {
        return ArrayList()
    }

    @JvmStatic
    fun <T> newArrayList(vararg es: T): ArrayList<T> {
        return ArrayList(es.toList())
    }

    @JvmStatic
    fun <T> newArrayList(iter: Iterable<T>): ArrayList<T> {
        val al = ArrayList<T>()
        iter.forEach {
            al += it
        }
        return al
    }

    @JvmStatic
    fun <T> newArrayList(iter: Iterator<T>): ArrayList<T> {
        val al = ArrayList<T>()
        iter.forEach {
            al += it
        }
        return al
    }

    @JvmStatic
    fun <T> newArrayListWithExpectedSize(size: Int): ArrayList<T> {
        return ArrayList(computeArrayListCapacity(size))
    }

    @JvmStatic
    fun <T> newArrayListWithCapacity(size: Int): ArrayList<T> {
        return ArrayList(size)
    }

    private fun computeArrayListCapacity(arraySize: Int): Int {
        return saturatedCast(5L + arraySize + arraySize / 10)
    }

    private fun saturatedCast(value: Long): Int {
        if (value > Int.MAX_VALUE) {
            return Int.MAX_VALUE
        }
        return if (value < Int.MIN_VALUE) {
            Int.MIN_VALUE
        } else value.toInt()
    }


}

object Maps{

    @JvmStatic
    fun <A, B> newHashMap(): HashMap<A, B> {
        return java.util.HashMap<A, B>()
    }

    @JvmStatic
    fun <A, B> newHashMap(from: Map<A, B>): HashMap<A, B> {
        return java.util.HashMap<A, B>(from)
    }

}

//TreeMap<Integer, TreeMap<Integer,Integer>>
class TreeBasedTable<R, C, V> {
    private val map = TreeMap<R, TreeMap<C, V>>()

    fun put(row: R, col: C, v: V) {
        val rowmap = map.getOrPut(row) { TreeMap() }
        rowmap[col] = v
    }

    /**
     * Returns the number of row key / column key / value mappings in the table.
     */
    fun size(): Int {
        var count = 0
        map.values.forEach { count += it.size }
        return count
    }

    fun rowKeySet(): Set<R> {
        return map.keys.toSet()
    }

    fun row(row: R): TreeMap<C, V>? {
        return map[row]
    }
}
