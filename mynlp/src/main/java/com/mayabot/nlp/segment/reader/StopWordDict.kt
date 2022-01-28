package com.mayabot.nlp.segment.reader

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.MynlpEnv
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
    /**
     * 判断当前输入word是否是停用词。true表示是停用词
     */
    fun contains(word: String): Boolean
}


/**
 * 停用词词典,基于DAT的实现
 *
 * 可以动态新增、删减停用词。修改后需要[rebuild]操作。
 *
 * @author jimichan
 */
class DefaultStopWordDict : StopWordDict {

    // value true 表示是停用词，false 表示不是停用词
    private val map: TreeMap<String, Boolean> = TreeMap()

    private var dat: DoubleArrayTrieMap<Boolean> = DoubleArrayTrieMap(
        TreeMap<String, Boolean>().apply { put("This Is Empty Flag", true) })

    private var isEmpty = true

    fun clear() {
        map.clear()
    }

    fun rebuild() {
        if (map.isEmpty()) {
            isEmpty = true
            return
        }

        isEmpty = false

        dat = DoubleArrayTrieMap(map)
    }

    fun add(words: Set<String>) {
        words.forEach {
            map[it] = true
        }
    }

    fun add(word: String) {
        map[word] = true
    }

    fun addNoStop(word: String) {
        map[word] = false
    }

    fun remove(word: String) {
        map.remove(word)
    }

    override fun contains(word: String): Boolean {
        return if (isEmpty) {
            false
        } else {
            return dat.get(word) ?: false
        }
    }

}

/**
 * 停用词词典,从系统中加载停用词词典
 *
 * @author jimichan
 */
@Singleton
class SystemStopWordDict constructor(val env: MynlpEnv) : StopWordDict {

    private val stopDict = DefaultStopWordDict()

    init {
        stopDict.add(loadSystemStopword(env))
        stopDict.rebuild()
    }

    override fun contains(word: String): Boolean {
        return stopDict.contains(word)
    }

    companion object {

        @JvmStatic
        fun loadSystemStopword(env: MynlpEnv): Set<String> {

            fun readStopDict(env: MynlpEnv): Set<String> {
                try {
                    val resource = env.tryLoadResource(StopWordDictPath)

                    resource?.let { re ->
                        return re.inputStream().bufferedReader().readLines().asSequence()
                            .map { it.trim() }.filter { it.isNotBlank() }.toSet()

                    }
                } catch (e: Exception) {
                    Mynlp.logger.error("", e)
                }

                return emptySet()
            }

            val wordSet = readStopDict(env).toMutableSet()

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

    }

}
