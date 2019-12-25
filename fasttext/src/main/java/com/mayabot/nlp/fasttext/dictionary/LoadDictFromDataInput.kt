package com.mayabot.nlp.fasttext.dictionary

import com.mayabot.nlp.fasttext.blas.AutoDataInput
import java.io.IOException
import java.util.ArrayList


//@Throws(IOException::class)
//fun load(buffer: AutoDataInput): Dictionary {
//    // wordList.clear();
//    // word2int_.clear();
//
//    size = buffer.readInt()
//    nwords = buffer.readInt()
//    nlabels = buffer.readInt()
//    ntokens = buffer.readLong()
//    pruneidxSize = buffer.readLong()
//
//    //        word_hash_2_id = new LongIntScatterMap(size_);
//    wordList = ArrayList(size)
//
//    //size 189997 18万的词汇
//    //val byteArray = ByteArray(1024)
//    for (i in 0 until size) {
//        val e = Entry(buffer.readUTF(), buffer.readLong(), EntryType.fromValue(buffer.readUnsignedByte().toInt()))
//        wordList.add(e)
//        word_hash_2_id[find(e.word)] = i
//    }
//
//    pruneidx.clear()
//    for (i in 0 until pruneidxSize) {
//        val first = buffer.readInt()
//        val second = buffer.readInt()
//        pruneidx.put(first, second)
//    }
//
//    initTableDiscard()
//    //if (ModelName.cbow == args_.model || ModelName.sg == args_.model) {
//    initNgrams()
//    //}
//    return this
//}
