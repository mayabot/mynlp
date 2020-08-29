package com.mayabot.nlp.common.hash

object MurmurHash3Utils {

    fun hashBytes(byteArray: ByteArray, offset: Int, length: Int, seed: Long = 0L, hash: MurmurHash3.Hash128 = MurmurHash3.Hash128()): MurmurHash3.Hash128 {
        return MurmurHash3.hash128(byteArray, offset, length, seed, hash)
    }

    fun hashBytes(byteArray: ByteArray): MurmurHash3.Hash128 {
        return this.hashBytes(byteArray, 0, byteArray.size)
    }

    fun hashString(text: String): Long {
        val bytes = text.toByteArray(Charsets.UTF_8)
        return hashBytes(bytes).h1
    }

}

fun String.murmur3(): Long {
    return MurmurHash3Utils.hashString(this)
}
