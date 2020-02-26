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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


/**
 * 通用感知机训练器
 * @param featureSet 特征集合
 * @param labelCount 标签的数量
 * @param trainSource 训练样例列表
 * @param evaluateScript 评估运行器
 * @param maxIter 迭代轮数
 * @param decodeQuickModel_ 是否启用快速解码(词性标注时启用)
 */
class PerceptronTrainer(
        private val featureSet: FeatureSet,
        private val labelCount: Int,
        private val trainSource: List<TrainSample>,
        private val evaluateScript: (iter: Int, perceptron: PerceptronModel) -> Unit,
        private val maxIter: Int,
        private val decodeQuickModel_: Boolean) {

    init {
        // 训练的时候FeatureSet里面是必须要有keys原文的。
        check(featureSet.keys != null)
    }

    private fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int): PerceptronModelImpl {
        return PerceptronModelImpl(
                featureSet, labelCount
        ).apply {
            this.decodeQuickMode(decodeQuickModel_)
        }
    }

    private fun buildPerceptronModel(featureSet: FeatureSet, labelCount: Int, parameter: FloatArray): PerceptronModelImpl {
        return PerceptronModelImpl(
                featureSet, labelCount, parameter
        ).apply {
            this.decodeQuickMode(decodeQuickModel_)
        }
    }

    /**
     * 默认多线程训练。
     * @param threadNumber 线程数。threadNumber=1 时平均感知机
     */
    @JvmOverloads
    fun train(threadNumber: Int = Runtime.getRuntime().availableProcessors() - 1): PerceptronModel {
        return if (threadNumber == 1) {
            trainOneThread()
        } else {
            trainParallel(threadNumber)
        }
    }

    /**
     * 单线程训练
     */
    private fun trainOneThread(): PerceptronModel {
        val model = buildPerceptronModel(
                featureSet, labelCount
        )

        //应该是权重的总和 最后要平均？
        val total = DoubleArray(model.parameterSize())
        //时间戳 每个正确预测的存活时间
        val timestamp = IntArray(model.parameterSize())
        var current = 0//第N次更新

        for (k in 1..maxIter) {
            val t1 = System.currentTimeMillis()
            println("\n#ITER $k/$maxIter")

            print("Process 0%")
            var per = 0
            trainSource.forEach {
                current++

                model.update(it, total, timestamp, current)

                per++

                if (per % 5000 == 0) {
                    print("\rProcess ${"%.2f".format((per * 100.0 / trainSource.size))}%")
                }
            }
            print("\r")
            val t2 = System.currentTimeMillis()

            println("train use ${t2 - t1} ms\n")

            // 备份参数
            val back = model.parameter.copyOf(model.parameterSize())
            model.average(total, timestamp, current)
            // 运行评估
            evaluateScript(k, model)
            model.parameter = back
        }

        //
        model.average(total, timestamp, current)
        return model
    }

    /**
     * 多线程版本
     */
    private fun trainParallel(threadNumber: Int): PerceptronModel {
        // val size = featureSet.size() * labelCount
        val modelArray = Array(threadNumber) {
            buildPerceptronModel(featureSet, labelCount)
        }

        val executor = Executors.newFixedThreadPool(threadNumber)

        val parts = trainSource.chunked((trainSource.size * 1.0 / threadNumber).toInt() + 1)

        for (k in 1..maxIter) {
            println("#ITER $k/$maxIter")
            val t1 = System.currentTimeMillis()
            val countDownLatch = CountDownLatch(threadNumber)
            for (s in 0 until threadNumber) {
                executor.submit {
                    try {
                        val list = parts[s]
                        var count = 0

                        if (s == 0) {
                            print("Process 0%")
                        }

                        list.forEach { d ->
                            modelArray[s].update(d)
                            count++

                            if (s == 0 && count % 5000 == 0) {
                                println("\rProcess ${"%.2f".format((count * 100.0 / list.size))}%")
                            }
                        }

                        if (s == 0) {
                            print("\r")
                        }

                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }
            countDownLatch.await()

            //把第二个开始的模型的参数全部和第一个平均
            val first = modelArray.first().parameter
            for (i in 1 until modelArray.size) {
                val the = modelArray[i].parameter
                for (j in 0 until first.size) {
                    first[j] += the[j]
                }
            }

            for (j in 0 until first.size) {
                first[j] = first[j] / modelArray.size
            }


            val t2 = System.currentTimeMillis()

            println("use ${t2 - t1} ms\n")
            evaluateScript(k, modelArray.first())
        }

        executor.shutdownNow()

        return buildPerceptronModel(
                featureSet,
                labelCount,
                modelArray.first().parameter
        )

    }
}

