package com.mayabot.nlp.perceptron


import com.mayabot.nlp.common.FastStringBuilder
import com.mayabot.nlp.common.hppc.IntArrayList
import java.io.File

/**
 * PerceptronRunner
 * @author jimi
 */
class PerceptronRunner<E, InputSequence>
@JvmOverloads
constructor(
        val definition: PerceptronDefinition<E, InputSequence>,
        val convertChar: Boolean = true
) {

    fun decodeModel(model: PerceptronModel, input: InputSequence): List<String> {
        val sentence = prepare(input)
        val vectorSequence = inputSeq2VectorSequence(sentence, model.featureSet())
        val pre = model.decode(vectorSequence)
        return pre.map { labels[it] }
    }

    @JvmOverloads
    fun decode(model: PerceptronModel, input: InputSequence, prepare: Boolean=true): IntArray {
        val sentence = if(prepare) prepare(input) else input
        val vectorSequence = inputSeq2VectorSequence(sentence, model.featureSet())
        val pre = model.decode(vectorSequence)
        return pre
    }

    fun learnModel(model: PerceptronModel, sample: String) {
        val id = makeSureFeatureSet(sample, model.featureSet())
        model.makeSureParameter(id)
        val x = sampleText2TrainSample(sample, model.featureSet())
        model.onlineLearn(x)
    }

    /**
     *  评估
     */
    fun evaluateModel(model: PerceptronModel, file: File): EvaluateResult {
        val evaluateSampleList =  files(file).flatMap { it.readLines() }
            val function = definition.evaluateFunction(model) ?: EvaluateFunction { samples ->
                simpleEvaluate(model, samples)
            }
        return function.evaluate(evaluateSampleList)
    }

    /**
     * 训练一个感知机模型
     */
    @JvmOverloads
    fun train(trainFile: File,
              evaluateFile: File?,
              iter: Int,
              threadNum: Int,
              quickDecode: Boolean = false)
            : PerceptronModel {

        val trainFiles = files(trainFile)

        //构建FeatureSet
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val featureSet = buildFeatureSet(trainFiles.asSequence().map { it.readLines() })
        println("构建FeatureSet耗时 ${System.currentTimeMillis() - t1} MS, 包含${featureSet.size()}个特征")

        //计算有多少行
        var lineCountLocal = 0
        trainFiles.forEach { file ->
            file.forEachLine { line ->
                if (line.isNotBlank()) {
                    lineCountLocal++
                }
            }
        }
        val lineCount = lineCountLocal

        val sampleList = ArrayList<TrainSample>(lineCount)
        // 加载样例
        val t2 = System.currentTimeMillis()
        trainFiles.forEach { file ->
            file.forEachLine { line ->
                sampleList += sampleText2TrainSample(line, featureSet)
                if (sampleList.size % 2000 == 0) {
                    println("Load ${sampleList.size}/$lineCount")
                }
            }
        }
        println("加载TrainSample耗时 ${System.currentTimeMillis() - t2} MS, 包含${sampleList.size}个样例")

        val evaluateSampleList = if (evaluateFile == null) emptyList() else files(evaluateFile).flatMap { it.readLines() }

        println("Start train ...")

        val trainer = PerceptronTrainer(
                featureSet,
                labels.size,
                sampleList,
                { id, it ->
                    val model = it

                    val function = definition.evaluateFunction(it) ?: EvaluateFunction { samples ->
                        simpleEvaluate(model, samples)
                    }

                    if (evaluateSampleList.isNotEmpty()) {
                        val r = function.evaluate(evaluateSampleList)
                        println("Evaluate Iter $id $r")
                    }
                }, iter, quickDecode)

        return trainer.train(threadNum)
    }

    /**
     * 标签列表。如 B M E S
     */
    private val labels = definition.labels()
    private val featureMaxSize = definition.featureMaxSize()

    private fun parseAnnotateText(text: String): List<Pair<E, String>> = definition.parseAnnotateText(text)

    /**
     * 把列表转换为InputSequence实际的容器对象，有些是原生char数组，有些就是list。
     * 在train和online learn时调用。
     * 输入[list]是标注好的数据，世/B 界/E 你/B 好/E。
     * 这个函数，把list转换为 "世界你好"，这种原始形式，这也是将来模型去对decode的数据类型。
     * 如分词，就是String（原始文本）
     */
    private fun inputList2InputSeq(list: List<E>) = definition.inputList2InputSeq(list)

    /**
     * 特征工程函数
     *
     * 每次[buffer]在使用之前需要调用[buffer].clear()。
     * 每次填充完buffer后，需要调用[emit]进行发射。
     *
     */
    private fun featureFunction(sentence: InputSequence,
                                size: Int,
                                position: Int,
                                buffer: FastStringBuilder,
                                emit: () -> Unit) = definition.featureFunction(
            sentence, size, position, buffer, emit
    )

    //-----------------------------------------------------//

    /**
     * 特征Buffer，预订最大特征字符串的长度
     */
    private fun buffer() = FastStringBuilder(featureMaxSize)

    private val labelMap: Map<String, Int> = labels.zip((labels.indices).toList()).toMap()

    /**
     * [label]对应的在[labels]里面的下标。
     */
    private fun labelIndex(label: String): Int {
        return labelMap.getValue(label)
    }

//    /**
//     * 加载训练语料的时候可以预处理InputSequence
//     *
//     * 子类可以覆盖实现.
//     */
//    private fun preProcessInputSequence(sentence: InputSequence,conv: Boolean=true) {
//        if (convertChar && sentence is CharArray && conv) {
//            CharNormUtils.convert(sentence)
//        }
//    }

    private fun prepare(sentence: InputSequence): InputSequence{
        return definition.preProcessInputSequence(sentence)
    }

    private fun oriInputFromSample(sample: String): InputSequence {
        val example = parseAnnotateText(sample).map { it.first }
        return inputList2InputSeq(example)
    }

    private fun makeSureFeatureSet(sample: String, featureSet: FeatureSet): Int {
        var max = 0
        val input = prepare(oriInputFromSample(sample))
        if (input is CharArray) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    val fid = featureSet.featureId(buffer)
                    if (fid < 0) {
                        val id = featureSet.newExtId(buffer.toString())
                        if (id > max) {
                            max = id
                        }
                    }
                }
            }
        } else if (input is List<*>) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    val fid = featureSet.featureId(buffer)
                    if (fid < 0) {
                        val id = featureSet.newExtId(buffer.toString())
                        if (id > max) {
                            max = id
                        }
                    }
                }
            }
        }
        return max

    }

    private fun files(file: File) = if (file.isFile) listOf(file) else file.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }.toList()

    /**
     * 默认简单的评估实现
     */
    private fun simpleEvaluate(model: PerceptronModel, sample: List<String>): EvaluateResult {
        val testSamples = sample.map { sampleText2TrainSample(it, model.featureSet()) }
        return simpleEvaluate(model, testSamples)
    }


    private fun buildFeatureSet(sampleBlock: Sequence<List<String>>): FeatureSet {
        val builder = DATFeatureSetBuilder(labels.size)

        sampleBlock.forEach { samples ->
            samples.forEach { sample ->
                val seq = prepare(oriInputFromSample(sample))
                inputSeq2FeatureSet(seq, builder)
            }
        }

        return builder.build()
    }

    private fun sampleText2TrainSample(text: String, featureSet: FeatureSet): TrainSample {
        val list = parseAnnotateText(text)
        val inputList = prepare(inputList2InputSeq(list.map { it.first }))

        val labelList = list.map { labelIndex(it.second) }.toIntArray()

        return TrainSample(
                inputSeq2VectorSequence(inputList, featureSet),
                labelList
        )

    }

    private fun inputSeq2VectorSequence(input: InputSequence, featureSet: FeatureSet): FeatureVectorSequence {
        if (input is CharArray) {
            val buffer = buffer()
            val size = input.size
            val out = ArrayList<FeatureVector>(input.size)

            for (i in 0 until input.size) {
                val vector = IntArrayList()
                featureFunction(input, size, i, buffer) {
                    val id = featureSet.featureId(buffer)
                    if (id >= 0) {
                        vector.add(featureSet.featureId(buffer))
                    }
                }
                vector.add(0)
                out += vector
            }
            return out
        } else if (input is List<*>) {
            val buffer = buffer()
            val size = input.size
            val out = ArrayList<FeatureVector>(input.size)

            for (i in 0 until input.size) {
                val vector = IntArrayList()
                featureFunction(input, size, i, buffer) {
                    val id = featureSet.featureId(buffer)
                    if (id >= 0) {
                        vector.add(featureSet.featureId(buffer))
                    }
                }
                vector.add(0)
                out += vector
            }
            return out
        }

        throw RuntimeException("support CharArray or List")
    }

    private fun inputSeq2FeatureSet(input: InputSequence, builder: DATFeatureSetBuilder) {
        if (input is CharArray) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    builder.put(buffer.toString())
                }
            }
        } else if (input is List<*>) {
            val size = input.size
            val buffer = buffer()
            for (i in 0 until input.size) {
                featureFunction(input, size, i, buffer) {
                    builder.put(buffer.toString())
                }
            }
        }
    }
}
