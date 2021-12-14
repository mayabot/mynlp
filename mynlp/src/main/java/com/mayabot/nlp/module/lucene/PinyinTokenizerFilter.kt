package com.mayabot.nlp.module.lucene

import com.mayabot.nlp.module.pinyin.PinyinService
import com.mayabot.nlp.module.pinyin.model.PinyinFuzzy.fuzzy2
import com.mayabot.nlp.module.pinyin.model.SimplePinyin
import org.apache.lucene.analysis.TokenStream

class PinyinTokenizerFilter(
    input: TokenStream,
    private val includeSelf: Boolean,
    private val fuzzy: Boolean,
    private val head: Boolean,
    private val pinyinService: PinyinService
) : BaseSynTokenFilter(input) {

    companion object {
        private val chaMap = mapOf<Char, String>(
            '一' to "1",
            '二' to "2",
            '三' to "3",
            '四' to "4",
            '五' to "5",
            '六' to "6",
            '七' to "7",
            '八' to "8",
            '九' to "9",
        )
    }

    override fun extend(item: CharSequence): List<String> {
        if (item.isEmpty()) {
            return emptyList()
        }
        // 大于1 肯定不是个中文单字，这个是我们的前提假设
        if (item.length > 1) {
            return if (head) {
                listOf(item.first().toString())
            } else {
                listOf(item.toString())
            }
        }

        val ch = item.first()
        val pinArray: Array<SimplePinyin>? = pinyinService.charPinyin(ch)
        if (pinArray != null) {
            val result = LinkedHashSet<String>()

            if (includeSelf) {
                result += ch.toString()
                if (chaMap.containsKey(ch)) {
                    result += chaMap.get(ch)!!
                }
            }

            if (fuzzy) {
                for (pinyin in pinArray) {
                    // 包括自己原始发音
                    for (x in fuzzy2(pinyin)) {
                        if (head) {
                            result.add(java.lang.String.valueOf(x.firstChar))
                        } else {
                            result.add(x.toString())
                        }
                    }
                }
            } else {
                for (pinyin in pinArray) {
                    if (head) {
                        result.add(java.lang.String.valueOf(pinyin.firstChar))
                    } else {
                        result.add(pinyin.toString())
                    }
                }
            }
            return result.toList()
        } else {
            return listOf(item.toString())
        }

    }

}