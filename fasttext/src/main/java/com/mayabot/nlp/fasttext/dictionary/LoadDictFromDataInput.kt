package com.mayabot.nlp.fasttext.dictionary

import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import java.io.IOException
import java.util.ArrayList


@Throws(IOException::class)
fun loadDictFromCppModel(args: ModelArgs,buffer: AutoDataInput): Dictionary {
    // wordList.clear();
    // word2int_.clear();

    val size = buffer.readInt()
    val nwords = buffer.readInt()
    val nlabels = buffer.readInt()
    val ntokens = buffer.readLong()
    val pruneidxSize = buffer.readLong()

    //        word_hash_2_id = new LongIntScatterMap(size_);
    val wordList = ArrayList<Entry>(size)

    for (i in 0 until size) {
        val e = Entry(buffer.readUTF(), buffer.readLong(), EntryType.fromValue(buffer.readUnsignedByte().toInt()))
        wordList.add(e)
    }

    val pruneidx = HashMap<Int,Int>()
    for (i in 0 until pruneidxSize) {
        val first = buffer.readInt()
        val second = buffer.readInt()
        pruneidx.put(first, second)
    }

    // 这里的实际WordHash2WordId是词数量的0.75倍
    val dict = Dictionary(args,
            FastWordMap(
            IntArray((size.toFloat()/0.75).toInt()) {-1},
            wordList),
            ntokens,
            nwords,
            nlabels
    )

    dict.initTableDiscard()
    dict.initNgrams()

    dict.onehotMap.initWordHash2WordId()

    return dict
}
