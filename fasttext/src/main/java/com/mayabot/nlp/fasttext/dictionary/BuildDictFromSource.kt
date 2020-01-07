package com.mayabot.nlp.fasttext.dictionary

import com.mayabot.nlp.fasttext.args.Args
import com.mayabot.nlp.fasttext.train.SampleLine
import com.mayabot.nlp.fasttext.utils.logger
import com.mayabot.nlp.fasttext.utils.loggerln


/**
 * 读取分析原始语料，语料单词直接空格
 *
 * @param trainExampleSource 训练文件
 * @throws Exception
 */
@ExperimentalUnsignedTypes
@Throws(Exception::class)
fun buildFromFile(args: Args,
                  sources: List<Iterable<SampleLine>>,
                  maxVocabSize: Int = 500000,
                  initWordListSize: Int = 5000
): Dictionary {


    val builder = DictionaryBuilder(args.label, maxVocabSize, initWordListSize)

    val mmm = 0.75 * maxVocabSize


    var lastMinThreshold = 1L

    loggerln("Read file build dictionary ...")

    var thresholdCount = 0
    fun thresholdBuilder() {
        var minThreshold: Long = lastMinThreshold
        thresholdCount++
        while (builder.size > (mmm * 0.75f)) {
            val before = builder.size
            builder.threshold(minThreshold, minThreshold)
            lastMinThreshold = minThreshold
            loggerln("word size from ${before} to ${builder.size} , threshold min $minThreshold")
            minThreshold++
        }
    }

    for (source in sources) {
        for (sample in source) {
            sample.words.forEach { token ->
                builder.add(token)
                if (builder.ntokens % 1000000 == 0L) {
                    logger("\rRead " + builder.ntokens / 1000000 + "M words")
                }

                if (builder.size > mmm) {
                    thresholdBuilder()
                }
            }
            builder.add(EOS)
        }
    }

    // 系统级别的裁剪
    builder.threshold(args.minCount.toLong(), args.minCountLabel.toLong())

    // 塌缩
    builder.wordIdMap.collapseWordHash2Id()

//        val dictionary = builder.toDictionary(args)
//        dictionary.initTableDiscard()
//        dictionary.initNgrams()

    System.out.printf("\rRead %dM words\n", builder.ntokens / 1000000)
    loggerln("Number of words:  ${builder.nwords}")
    loggerln("Number of labels: ${builder.nlabels}")

    if (thresholdCount > 0) loggerln("Max threshold count: $lastMinThreshold")

    if (builder.wordIdMap.size == 0) {
        throw RuntimeException("Empty vocabulary. Try a smaller -minCount second.")
    }

    val dict = builder.toDictionary(args)
    loggerln("Number of wordHash2Id: ${dict.onehotMap.wordHash2WordId.size}")
    return dict

}
