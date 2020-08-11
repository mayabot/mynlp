package com.mayabot.nlp.segment.reader

import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieMap
import com.mayabot.nlp.common.injector.ImplementedBy
import com.mayabot.nlp.common.injector.Singleton
import java.util.*

/**
 * 系统默认自带的停用词表
 */
const val StopWordDictPath = "stopwords.txt"

/**
 * 格式
 * + word
 * - wordb
 * 用来控制对默认停用词表的增加和删除
 */
const val MergeStopWordDictPath = "merge_stopwords.txt"

/**
 * 停用词接口
 *
 * Guice默认注入SystemStopWordDict
 *
 * @author jimichan
 */
@ImplementedBy(SystemStopWordDict::class)
interface StopWordDict {
    fun contains(word: String): Boolean
    fun add(word: String)
    fun remove(word: String)
    fun commit()
}


/**
 * 停用词词典,基于DAT的实现
 *
 * 可以动态新增、删减停用词。修改后需要[commit]操作。
 *
 * @author jimichan
 */
class DefaultStopWordDict(set: Set<String>) : StopWordDict {

    private var stopWordSet = HashSet(set)

    private var dat: DoubleArrayTrieMap<Boolean> = DoubleArrayTrieMap(
            TreeMap<String, Boolean>().apply { put("This Is Empty Flag", true) })
    private var isEmpty = false

    init {
        commit()
    }

    override fun commit() {
        if (stopWordSet.isEmpty()) {
            isEmpty = true
            return
        }
        isEmpty = false
        val treeMap = TreeMap<String, Boolean>()
        stopWordSet.forEach {
            treeMap[it] = true
        }
        dat = DoubleArrayTrieMap(treeMap)
    }

    override fun add(word: String) {
        stopWordSet.add(word)
    }

    override fun remove(word: String) {
        stopWordSet.remove(word)
    }


    override fun contains(word: String) = dat.containsKey(word)

}

/**
 * 停用词词典,从系统中加载停用词词典
 *
 * @author jimichan
 */
@Singleton
class SystemStopWordDict constructor(val env: MynlpEnv) : StopWordDict {

    private val stopDict = DefaultStopWordDict(loadStopword())

    override fun contains(word: String): Boolean {
        return stopDict.contains(word)
    }

    override fun commit() {
        stopDict.commit()
    }

    override fun add(word: String) {
        stopDict.add(word)
    }

    override fun remove(word: String) {
        stopDict.remove(word)
    }

    private fun loadStopword(): Set<String> {
        val wordSet = loadStopword2().toMutableSet()

        // 如果存在merge_stopwords.txt资源的话
        val resource = env.tryLoadResource(MergeStopWordDictPath)
        resource?.let { re ->
            re.inputStream().bufferedReader().readLines().forEach { line_ ->
                val line = line_.trim()
                if (line.isNotBlank()) {
                    if (line.startsWith("+")) {
                        wordSet += line.substring(1).trim()
                    } else if (line.startsWith("-")) {
                        wordSet -= line.substring(1).trim()
                    }
                }
            }
        }

        return wordSet
    }


    private fun loadStopword2(): Set<String> {

        try {
            val resource = env.tryLoadResource(StopWordDictPath)

            resource?.let { re ->
                return re.inputStream().bufferedReader().readLines().asSequence()
                        .map { it.trim() }.filter { it.isNotBlank() }.toSet()

            }
        } catch (e: Exception) {
            Mynlps.logger.error("", e)
        }

        return emptySet()
    }
}
