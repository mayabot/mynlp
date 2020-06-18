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

import com.mayabot.nlp.utils.CharNormUtils


/**
 * 简单评估。只考虑标签相等性
 */
fun simpleEvaluate(model: PerceptronModel, samples: List<TrainSample>): EvaluateResult {

    var total = 0
    var right = 0

    samples.forEach { sample ->
        total += sample.label.size
        val gold = sample.label
        val pred = model.decode(sample.featureSequence)
        for (i in sample.label.indices) {
            if (gold[i] == pred[i]) {
                right++
            }
        }
    }

    return EvaluateResult(total, total, right)
}

fun segmentEvaluateFunction(
        textSegment:(String)->List<String>,
        split:String = "﹍",
        verbose:Boolean = false):EvaluateFunction{


    return EvaluateFunction{evaluateSample->
        // int goldTotal = 0, predTotal = 0, correct = 0;
        var goldTotal = 0
        var predTotal = 0
        var correct = 0


        //val splitter = Splitter.on(split).omitEmptyStrings()

        if (verbose) System.out.print("Evaluating 0%")

        val t1 = System.currentTimeMillis()

        var count = 0
        for (line in evaluateSample) {

            val wordArray = CharNormUtils.convert(line).split(split).filter { it.isNotBlank() }
            goldTotal += wordArray.size

            val text = wordArray.joinToString(separator = "")
            val predArray = textSegment(text)
            predTotal += predArray.size

            correct += wordCorrect(wordArray, predArray)

            count++

            if (count % 2000 == 0) {
                if(verbose) System.out.print("\rEvaluating ${"%.2f".format(count * 100.0 / evaluateSample.size)}%")
            }

        }

        fun prf(goldTotal: Int, predTotal: Int, correct: Int): DoubleArray {
            val precision = correct * 100.0 / predTotal
            val recall = correct * 100.0 / goldTotal
            val performance = DoubleArray(3)
            performance[0] = precision
            performance[1] = recall
            performance[2] = 2.0 * precision * recall / (precision + recall)
            return performance
        }

        val result = prf(goldTotal, predTotal, correct)

        if(verbose) System.out.print("\r")

        val t2 = System.currentTimeMillis()

        if(verbose) System.out.println("正确率(P) %.2f , 召回率(R) %.2f , F1 %.2f".format(result[0], result[1], result[2]))
        if(verbose) println("Evaluate use time ${t2 - t1} ms")

        EvaluateResult(result[0].toFloat(),result[1].toFloat())
    }
}

/**
 * 计算分词正确的词数
 */
fun wordCorrect(gold: List<String>, pred: List<String>): Int {
    var goldIndex = 0
    var predIndex = 0
    var goldLen = 0
    var predLen = 0

    var correct = 0
    while (goldIndex < gold.size && predIndex < pred.size) {
        if (goldLen == predLen) {
            if (gold[goldIndex] == pred[predIndex]) {
                correct++
                goldLen += gold[goldIndex].length
                predLen += gold[goldIndex].length
                goldIndex++
                predIndex++
            } else {
                goldLen += gold[goldIndex].length
                predLen += pred[predIndex].length
                goldIndex++
                predIndex++
            }
        } else if (goldLen < predLen) {
            goldLen += gold[goldIndex].length
            goldIndex++
        } else {
            predLen += pred[predIndex].length
            predIndex++
        }
    }
    return correct
}