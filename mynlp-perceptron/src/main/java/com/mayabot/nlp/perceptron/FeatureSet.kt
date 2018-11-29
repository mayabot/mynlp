package com.mayabot.nlp.perceptron

import com.mayabot.nlp.collection.dat.DATArrayIndex
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream

/**
 * 特征集。主要功能是查询feature对应的Id.
 *
 */
class FeatureSet(
        private val dat: DATArrayIndex,
        var keys: List<String>?
) {

    /**
     * 返回一个特征对应的ID
     * @return -1表示特征不存在
     */
    fun featureId(feature: String) = dat.indexOf(feature)


    /**
     * 特征大小
     * @return 特征集合的大小
     */
    fun size() = dat.size()

    /**
     * 保存到文件
     */
    fun save(datFile: File, textFile: File?) {
        datFile.outputStream().buffered().use {
            val out = DataOutputStream(it)
            dat.write(out)
        }

        val keys = this.keys
        if (keys != null && textFile != null) {
            textFile.bufferedWriter().use { writer ->
                keys.forEach { line ->
                    writer.write(line + "\n")
                }
            }
        }
    }

    companion object {
        /**
         * 只读取DAT文件
         */
        @JvmStatic
        fun read(datInput: InputStream): FeatureSet {
            return datInput.use {
                val datArray = DATArrayIndex(DataInputStream(it))
                FeatureSet(datArray, null)
            }
        }

        @JvmStatic
        fun read(datInput: InputStream, textInput: InputStream): FeatureSet {
            return datInput.use {
                val datArray = DATArrayIndex(DataInputStream(it))
                val lines = textInput.use { x ->
                    x.bufferedReader().readLines()
                }
                FeatureSet(datArray, lines)
            }
        }

        @JvmStatic
        fun readFromText(textInput: InputStream): FeatureSet {
            return textInput.use {
                val reader = textInput.bufferedReader()
                val list = reader.readLines()

                val datArray = DATArrayIndex(list)
                FeatureSet(datArray, list)
            }
        }
    }

}

/**
 * DAT的特征集合构建器
 */
class DATFeatureSetBuilder(labelCount: Int) {

    val keys = HashSet<String>()


    init {
        // Hanlp需要从 0=< <= labelCount 上站位 占用labelCount+1个位置
        // 我们要保证这个排在前面
        for (i in 0..labelCount) {
            keys.add("\u0000\u0001BL=$i")
        }

    }
    fun put(feature: String) {
        keys.add(feature)
    }

    fun build(): FeatureSet {
        val list = keys.sorted()
        return FeatureSet(DATArrayIndex(list), list)
    }
}


