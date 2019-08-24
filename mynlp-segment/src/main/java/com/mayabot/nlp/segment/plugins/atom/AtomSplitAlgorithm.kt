package com.mayabot.nlp.segment.plugins.atom

import com.google.inject.ImplementedBy
import com.google.inject.Inject
import com.google.inject.Singleton
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap
import com.mayabot.nlp.collection.dat.FastDatCharSet
import com.mayabot.nlp.segment.Nature
import com.mayabot.nlp.segment.WordSplitAlgorithm
import com.mayabot.nlp.segment.common.BaseSegmentComponent
import com.mayabot.nlp.segment.common.String2
import com.mayabot.nlp.segment.wordnet.Wordnet
import java.util.*
import java.util.regex.Pattern

// DoubleArrayTrieStringIntMap

@ImplementedBy(DefaultAtomSplitAlgorithmTemplateProvider::class)
interface AtomSplitAlgorithmTemplateProvider{
    fun load():DoubleArrayTrieStringIntMap
}

@Singleton
class DefaultAtomSplitAlgorithmTemplateProvider:AtomSplitAlgorithmTemplateProvider{

    private val defaultTemplates = defaultTemplates()

    override fun load(): DoubleArrayTrieStringIntMap {
        Mynlps.logger.info("Load Default AtomSplitAlgorithm Template")
        return defaultTemplates
    }

}

/**
 * 高性能多模式识别。
 *
 * 解决了性能难题，如何只扫描文本一遍的情况下，识别多种字符串模式？
 *
 * @author jimichan
 */
@Singleton
class AtomSplitAlgorithm @Inject constructor(
        templateProvider: AtomSplitAlgorithmTemplateProvider)
    : BaseSegmentComponent(LEVEL2), WordSplitAlgorithm {

    val dat: DoubleArrayTrieStringIntMap = templateProvider.load()

    //CharScatterSet 5000万次查询耗时40ms
    private val chineseNumSet = FastDatCharSet(
            '零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '两',
            '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖', '拾',
            '十', '百', '千', '万', '亿'
    )

    private val emailPattern = Pattern.compile("[NA]+@[NA]+NA+")

    //fm101.1 iphone7 fm-981 a-b-c
    private val xPattern = Pattern.compile("A+[N\\-][N\\-A]*")

    override fun fill(wordnet: Wordnet) {
        val chars = wordnet.charArray

        //先扫描，判断是否包含数字、英文字母等必要元素
        var foundNum = false
        var foundAlpha = false
        var foundAt = false

        for (i in 0 until chars.size) {

            val c = chars[i]

            if (c < '{') {
                if (c >= '0' && c <= '9') {
                    foundNum = true
                } else if ((c >= 'a' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    foundAlpha = true
                }
            } else {
                if (chineseNumSet.contains(c)) {
                    foundNum = true
                }
            }
            if (foundAlpha || foundNum) {
                break
            }
        }

        var foundBigX = false
        if (foundNum || foundAlpha) {
            val newChars = Arrays.copyOf(chars, chars.size)
            for (i in 0 until chars.size) {
                var c = chars[i]
                if (c < '{') {
                    if (c >= '0' && c <= '9') {
                        newChars[i] = 'N'
                        continue
                    } else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
                        newChars[i] = 'A'
                        // _也算字母
                        continue
                    } else if (c == '.') {
                        // 把点也归一化为数字N的一种形式
                        newChars[i] = 'N'
                    } else if (c == '@') {
                        foundAt = true
                    }
                } else {
                    if (chineseNumSet.contains(c)) {
                        newChars[i] = 'Z'
                        continue
                    }
                }
            }

//            println(String(newChars))

            val match = dat.matchLong(newChars, 0)

            var bigXEnd = -1

            while (match.next()) {
                val type = match.value
                val offset = match.begin
                val length = match.length

                when (type) {
                    0 -> {
                        wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.t)
                    }
                    1 -> {
                        if (length == 1 && chars[offset] == '.') {

                        } else {
                            if (offset == bigXEnd) {
                                foundBigX = true
                            }
                            wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.m)
                        }
                    }
                    2 -> wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.mq)
                    3 -> {
                        wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.x)
                        bigXEnd = offset + length
                    }
                    4 -> {
                        //连接符号
                        if (offset == bigXEnd) {
                            foundBigX = true
                        }
                    }
                }
            }

            if (foundAt) {
                var matcher = emailPattern.matcher(String2(newChars))
                while (matcher.find()) {
                    wordnet.put(matcher.start(), matcher.end() - matcher.start()).setAbsWordNatureAndFreq(Nature.x)
                }
            }

            if (foundBigX) {
                var matcher = xPattern.matcher(String2(newChars))
                while (matcher.find()) {
                    wordnet.put(matcher.start(), matcher.end() - matcher.start()).setAbsWordNatureAndFreq(Nature.x)
                }
            }
        }

    }

}
