package com.mayabot.mynlp.fasttext


import com.carrotsearch.hppc.IntArrayList
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.util.*

fun String.toFile() = File(this)

fun File.lines() = Files.lines(this.toPath())!!

fun File.forEachLine(action: (String) -> Unit) = Files.lines(this.toPath()).forEach(action)

fun File.firstLine(): String? = Files.lines(this.toPath()).findFirst().orElse(null)


inline fun checkArgument(expression: Boolean) {
    if (!expression) {
        throw IllegalArgumentException()
    }
}


internal var sqrt = FloatArray(200000).apply {
    for(i in 0 until 200000){
        this[i] = Math.pow(i.toDouble(),0.5).toFloat()
    }
}

fun sqrt(d: Long): Float {
    return if (d < 200000) {
        sqrt[d.toInt()]
    } else Math.pow(d.toDouble(), 0.5).toFloat()
}

fun iota(data: IntArray) {
    for (i in data.indices) {
        data[i] = i
    }
}


fun swap(array: IntArray, i: Int, j: Int) {
    val x = array[i]
    array[i] = array[j]
    array[j] = x
}

fun swap(array: IntArrayList, i: Int, j: Int) {
    val x = array.get(i)
    array.set(i, array.get(j))
    array.set(j, x)
}


fun shuffle(array: IntArray, random: Random) {
    val size = array.size
    for (i in size - 1 downTo 2) {
        swap(array, i - 1, random.nextInt(i))
    }
}

fun shuffle(array: IntArrayList, random: Random) {
    val size = array.size()
    for (i in size - 1 downTo 2) {
        swap(array, i - 1, random.nextInt(i))
    }
}


object NormalizeText {
    /*
     * We categorize longer strings into the following buckets:
     *
     * 1. All punctuation-and-numeric. Things in this bucket get
     *    their numbers flattened, to prevent combinatorial explosions.
     *    They might be specific numbers, prices, etc.
     *
     * 2. All letters: case-flattened.
     *
     * 3. Mixed letters and numbers: a product ID? Flatten case and leave
     *    numbers alone.
     *
     * The case-normalization is state-machine-driven.
     */

    @JvmStatic
    fun normalize(str: String): String {

        var allNumeric = true
        var containsDigits = false

        for (i in 0 until str.length) {
            val c = str[i]

            containsDigits = containsDigits or Character.isDigit(c)

            if (!Character.isAlphabetic(c.toInt())) {
                allNumeric = false
                continue
            }
            if (!Character.isAlphabetic(c.toInt())) continue
            allNumeric = false
        }

        val flattenCase = true
        val flattenNum = allNumeric && containsDigits
        if (!flattenNum && !flattenCase) return str

        val chars = str.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (flattenNum && Character.isDigit(c)) {
                chars[i] = '0'
            }
            if (Character.isAlphabetic(c.toInt())) {
                chars[i] = Character.toLowerCase(c)
            }
        }
        return String(chars)
    }
}
