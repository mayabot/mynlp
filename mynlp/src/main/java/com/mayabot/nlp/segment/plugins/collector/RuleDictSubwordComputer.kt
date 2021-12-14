package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.Nature
import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath

/**
 * 规则词典
 * ABC => A/BC 切分
 * ABC => ABC 不切分
 * A?C => A/?C 通配切分
 */
class RuleDictSubwordComputer(val dictList: List<SubwordRuleDict>) : SubwordComputer {
    override fun run(term: WordTerm, wordnet: Wordnet, wordPath: Wordpath): Boolean {
        dictList.forEach {
            val mt = it.match(term.word)
            if (mt != null) {
                val word = term.word
                var offset = 0
                val sublist = ArrayList<WordTerm>(mt.size)
                mt.forEach { len ->
                    val row = wordnet.getRow(term.offset + offset)
                    val nature = row.get(len)?.nature ?: Nature.newWord
                    sublist += WordTerm(word.substring(offset, offset + len), nature, offset + term.offset)
                    offset += len
                }
                term.subword = sublist
                return true
            }
        }
        return false
    }
}

interface SubwordRuleDict {
    /**
     *
     * @return Int数组，每个int表示对应词的长度
     * ABC => ABC 不切分 返回[3]
     * A/?C 通配切分 返回 [1,2]
     * 返回 null表示没有匹配
     */
    fun match(word: String): IntArray?

}

class DefaultSubwordRuleDict : SubwordRuleDict {

    private val rules = ArrayList<String>()

    private val eqMap = HashMap<String, IntArray>()
    private val matchMap = ArrayList<Tongpei>()

    class Tongpei(
        val regex: Regex,
        val result: IntArray,
        val len: Int,
        val start: Char? = null,
        val end: Char? = null
    )

    fun addAll(rule: List<String>) {
        this.rules += rule
    }

    fun add(rule: String) {
        this.rules += rule
    }

    fun clear() {
        rules.clear()
    }

    fun rebuild() {
        // TODO 低效的实现
        eqMap.clear()
        matchMap.clear()

        rules.forEach { rule ->
            if (rule.contains("?")) {
                val withoutSplit = rule.replace("/", "")
                val regex = Regex(withoutSplit.replace("?", ".?"))
                val array = rule.split('/').map { it.length }.toIntArray()
                var start: Char? = withoutSplit.first()
                if (start == '?') {
                    start = null
                }
                var end: Char? = withoutSplit.last()
                if (end == '?') {
                    end = null
                }
                check(!(start == null && end == null)) { "$rule 不能头和尾都是通配符号" }

                matchMap += Tongpei(
                    regex = regex,
                    result = array,
                    len = withoutSplit.length,
                    start = start, end = end
                )

            } else {
                val sp = rule.split("/")
                val key = sp.joinToString(separator = "")
                eqMap[key] = sp.map { it.length }.toIntArray()
            }
        }
    }

    override fun match(word: String): IntArray? {
        if (eqMap.contains(word)) {
            return eqMap[word]
        }

        matchMap.forEach {
            if (
                (it.start != null && word.first() == it.start)
                || (it.end != null && word.last() == it.end)
            ) {
                if (it.regex.matches(word)) {
                    return it.result
                }
            }
        }
        return null
    }

}
