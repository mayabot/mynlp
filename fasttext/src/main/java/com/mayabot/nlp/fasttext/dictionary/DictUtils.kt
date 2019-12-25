@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package com.mayabot.nlp.fasttext.dictionary

import com.carrotsearch.hppc.IntArrayList

// The correct implementation of fnv should be:
// h = h ^ uint32_t(uint8_t(str[i]));
// Unfortunately, earlier version of fasttext used
// h = h ^ uint32_t(str[i]);
// which is undefined behavior (as char can be signed or unsigned).
// Since all fasttext models that were already released were trained
// using signed char, we fixed the hash function to make models
// compatible whatever compiler is used.
@ExperimentalUnsignedTypes

fun String.fnv1aHash() : UInt{
    var h = 2166136261u
    for (strByte in this.toByteArray()) {
        h =  (h xor strByte.toUInt())
        h = h * 16777619u
    }
    return h
}

fun isWhiteSpaceChar(ch: Char) = ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r'


val Empty_IntArrayList = IntArrayList(0)

data class Entry(
        val word: String,
        var count: Long,
        val type: EntryType
) {
    var subwords: IntArrayList = Empty_IntArrayList
}


enum class EntryType constructor(var value: Int) {

    word(0), label(1);

    override fun toString(): String {
        return if (value == 0) "word" else if (value == 1) "label" else "unknown"
    }

    companion object {

        internal var types = EntryType.values()

        @Throws(IllegalArgumentException::class)
        fun fromValue(value: Int): EntryType {
            try {
                return types[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown EntryType enum second :$value")
            }

        }
    }
}


//uint32_t Dictionary::hash(const std::string& str) const {
//    uint32_t h = 2166136261;
//    for (size_t i = 0; i < str.size(); i++) {
//        h = h ^ uint32_t(int8_t(str[i]));
//        h = h * 16777619;
//    }
//    return h;
//}
