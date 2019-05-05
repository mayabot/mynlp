/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.perceptron

import com.mayabot.nlp.collection.dat.DoubleArrayTrie
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream

/**
 * 特征集。主要功能是查询feature对应的Id.
 * 逻辑上类似
 * ["f1","f2","f3]
 * 那么 f1对应的Id是0，f2对应的Id=1
 * 每个特征都对应数组下标。
 * FeatureSet中每个特征都有一个唯一的Int编号。
 * 所谓特征就是字符串构成的特征，具体特征是什么有应用层自己去定义。
 * 在mynlp中为了计算性能，需要在第一遍扫描语料时，构建FeatureSet是要求对所有的feature进行排序，
 * 然后采用压缩性能更好的DAT结构来存储这些信息。
 * @author jimichan
 */
class FeatureSet(
        private val dat: DoubleArrayTrie,
        var keys: List<String>?
) {

    //预留的多一点，就不会有hash冲突
    var extMap = HashMap<String, Int>(1000)

    var nextId = dat.size()

    // 要搞两种模式，一个是训练的时候，就不动了一个是工作模式

    /**
     * 返回一个特征对应的ID
     * @return -1表示特征不存在
     */
    fun featureId(feature: String): Int {
        val id = dat.indexOf(feature)

        if (id >= 0) {
            return id
        }

        return if (extMap.isNotEmpty()) {
            extMap[feature] ?: -1
        } else {
            id
        }
    }

    fun featureId(feature: CharSequence): Int {
        val id = dat.indexOf(feature)

        if (id >= 0) {
            return id
        }
        return if (extMap.isNotEmpty()) {
            extMap[feature.toString()] ?: -1
        } else {
            id
        }
    }

    fun newExtId(feature: String): Int {
        if (dat.indexOf(feature) < 0 && !extMap.containsKey(feature)) {
            extMap[feature] = nextId++
            return extMap[feature]!!
        }

        return featureId(feature)
    }


    /**
     * 特征大小
     * @return 特征集合的大小
     */
    fun size() = dat.size() + extMap.size

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
                val datArray = DoubleArrayTrie(DataInputStream(it))
                FeatureSet(datArray, null)
            }
        }

        @JvmStatic
        fun read(datInput: InputStream, textInput: InputStream): FeatureSet {
            return datInput.use {
                val datArray = DoubleArrayTrie(DataInputStream(it))
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

                val datArray = DoubleArrayTrie(list)
                FeatureSet(datArray, list)
            }
        }
    }

}



