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

import com.mayabot.nlp.common.FastStringBuilder
import java.io.File
import java.io.InputStream

/**
 * 一个被训练的原始文本。解析为(X,Label)序列。
 * E为序列元素对象，一般为char，ner里面比较复杂的话，是一个是string+pos
 * [InputSequence] 模型去对decode的数据类型。E的容器对象，可能是个CharArray(E为char)，也可能是个List。
 * 如分词，就是String（原始文本），NER就是一个元素List。
 *
 * 比如 分词语料， "世界 你好" => 世/B 界/E 你/B 好/E
 */

interface PerceptronDefinition<E, InputSequence>{

    fun labels(): Array<String>

    fun featureMaxSize(): Int
    /**
     * 对标注的文本进行解析。
     * [text]是一个语料里面的标注格式文本。
     * 比如分词里面，标注文本是 "世界/你好"，那么这个函数，需要把它变成
     * [ 世/B 界/E 你/B 好/E ],这也是对语料标注的一个转换。
     * 在train和online learn时调用。
     */
     fun parseAnnotateText(text: String): List<Pair<E, String>>

    /**
     * 把列表转换为InputSequence实际的容器对象，有些是原生char数组，有些就是list。
     * 在train和online learn时调用。
     * 输入[list]是标注好的数据，世/B 界/E 你/B 好/E。
     * 这个函数，把list转换为 "世界你好"，这种原始形式，这也是将来模型去对decode的数据类型。
     * 如分词，就是String（原始文本）
     */
    fun inputList2InputSeq(list: List<E>): InputSequence

    fun preProcessInputSequence(input:InputSequence): InputSequence

    /**
     * 特征工程函数
     *
     * 每次[buffer]在使用之前需要调用[buffer].clear()。
     * 每次填充完buffer后，需要调用[emit]进行发射。
     *
     */

    fun featureFunction(sentence: InputSequence,
                                 size: Int,
                                 position: Int,
                                 buffer: FastStringBuilder,
                                 emit: () -> Unit)

    /**
     * 评估函数逻辑
     */
    fun evaluateFunction(perceptron: PerceptronModel) : EvaluateFunction?

    /**
     * 计算器对象
     */
    fun modelComputer(model: PerceptronModel): PerceptronModelComputer<E,InputSequence> {
        val computer = PerceptronComputer(this)
        return object :PerceptronModelComputer<E,InputSequence>{
            override fun decodeModel(input: InputSequence): List<String> {
                return computer.decodeModel(model,input)
            }

            override fun decode(input: InputSequence, prepare: Boolean): IntArray {
                return computer.decode(model,input,prepare)
            }

            override fun learnModel(sample: String) {
                computer.learnModel(model,sample)
            }

            override fun evaluateModel(file: File): EvaluateResult {
               return computer.evaluateModel(model,file)
            }
        }
    }

    /**
     * 模型训练入口
     */
    fun train(trainFile: File,
              evaluateFile: File?,
              iter: Int,
              threadNum: Int,
              quickDecode: Boolean = false)
            : PerceptronModel{
        return PerceptronComputer(this).train(trainFile,evaluateFile, iter, threadNum,quickDecode)
    }

}