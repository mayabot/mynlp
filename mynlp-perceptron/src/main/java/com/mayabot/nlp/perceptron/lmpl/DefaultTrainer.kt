package com.mayabot.nlp.perceptron.lmpl

import com.mayabot.nlp.algorithm.SecondOrderViterbi
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.perceptron.model.CostumisedPerceptron
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.Map.Entry

//abstract class DefaultParser<E, T, P>(val featureNumber: Int) : PerceptronDataParser<T, E> where E : Enum<E>, P : FeatureExtractor<T> {
//    abstract val featureMap: FeatureMap<String>
//    abstract val parameter: IntArray
//    abstract val extractor: P
//    override fun parse(source: MutableIterable<SequenceLabel<T>>?): PerceptronTrainer<T> {
//        source!!.forEach {
//            for (i in 0 until it.length()) {
////                extractor.featureExtract(it.sequence,i)
//            }
//        }
//
//
//        return null
//    }
//    //    override fun train(source: MutableIterable<SequenceLabel<E>>?): PerceptronModel<E> {//source should be shuffled
////        //source : dataSet  sequence: sample
////        val sequence = source
////        val total = DoubleArray(parameter.size)
////        val timestamp = IntArray(parameter.size)
////        var current = 0
////        for (iter in 0 until maxIterator){
////            source!!.forEach {
////                current++
////                val sequenceSize = it.sequence.size
////                val guessLabel = IntArray(sequenceSize)
////                //viterbi
////                for (i in 0 until sequenceSize){
//////                    val featureVector = it.getFeatureAt(i)
//////                    val goldFeature = IntArray(featureVector.size)
//////                    val predFeature = IntArray(featureVector.size)
////                }
////            }
////        }
////        return ModelIMPL()
////    }
//
//}
fun main(args: Array<String>) {
    val list = readConllFile("/Users/mei_chaofeng/conll/conll-2012/v4/data/train/data/chinese/annotations")
//    val list = readPukFile("/Users/mei_chaofeng/conll/conll-2012/v4/data/train/data/chinese/199801.txt")
    val builder = TrainerBuilder(list, DefaultFeatureExtractor(), arrayOf(0, 1, 2, 3).toIntArray(), 3)
    val trainer = builder.buildDefaultModel()
    val model = trainer.train()
    val resultSet = ArrayList<String>()
    resultSet.add(model.decode("我爱北京天安门和长城".toCharArray().toTypedArray()))
    resultSet.add(model.decode("还这个世界一片蓝天".toCharArray().toTypedArray()))
    resultSet.add(model.decode("小明想要一个女朋友".toCharArray().toTypedArray()))
    resultSet.add(model.decode("我们去炮台湾公园玩吧".toCharArray().toTypedArray()))
    resultSet.add(model.decode("老刘兴高采烈地来上班了".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("小明想要一个女朋友".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("小明想要一个女朋友".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("小明想要一个女朋友".toCharArray().toTypedArray()))

    resultSet.forEach {
        println(it)
    }
}

class StructuredPerceptronTrainer<T>(dataInput: MutableIterable<SequenceLabel<T>>,
                                     val extractor: FeatureExtractor<T>,
                                     tagSet: IntArray,
                                     val maxIter: Int) {
    private var dataSet: List<FeaturedSequenecLabel>
    val labelSet = tagSet
    private var featureMap: FeatureMap = FeatureMap(tagSet)
    lateinit var vectorSet: List<List<List<IntArray>>>
    private val model: CostumisedPerceptron<T>
//    private val viterbi = SecondOrderViterbi<Int, IntArray>(::ss1, ::mmm1, { tag, v -> })

    fun ss1(a: Entry<Int, Int>, b: Entry<Int, Int>): Double {
        return 0.0
    }

    fun mmm1(x: Int): Map<Int, Int> {
        return mapOf()
    }

    init {
        dataSet = dataInput.map {
            //sentence
            val sequence = it.sequence
            val temp = sequence.mapIndexed { index, t ->
                extractor.featureExtract(sequence, index, featureMap)
            }
            FeaturedSequenecLabel(temp, it.label)
        }
        println("feature extraction completed")
        model = CostumisedPerceptron(
                featureMap,
                FloatArray(featureMap.featureSize() * featureMap.tagSize()),
                extractor
        )
    }


    fun train(): CostumisedPerceptron<T> {
        //应该是权重的总和 最后要平均？
        val total = DoubleArray(model.parameter.size)
        //时间戳 每个正确预测的存活时间
        val timestamp = IntArray(model.parameter.size)
        var current = 0//第N次更新
        val size = dataSet.size
        for (iterate in 0 until maxIter) {
            if (iterate == 1)
                println(111)
            if (iterate == 2)
                println(111)
//            dataSet = dataSet.shuffled()
            dataSet.forEach {
                val length = it.size
                current++
                val guessLabel = IntArray(length)
                //FIXME
                viterbiDecode(it, guessLabel)
                for (i in 0 until length) {
                    //序号代替向量 替代数组
                    val featureVector = it.featureMatrix[i]
                    //正确标签
                    val goldFeature = IntArray(featureVector.size)
                    //预测标签
                    val predFeature = IntArray(featureVector.size)
                    for (j in 0 until featureVector.size - 1) {
                        //编号 * 4 + 正确编号 这是什么操作？ oook
                        goldFeature[j] = featureVector[j] * labelSet.size + it.label[i]
                        predFeature[j] = featureVector[j] * labelSet.size + guessLabel[i]
                    }
                    goldFeature[featureVector.size - 1] = (if (i == 0) labelSet.size else it.label[i - 1]) * labelSet.size + it.label[i]
                    predFeature[featureVector.size - 1] = (if (i == 0) labelSet.size else guessLabel[i - 1]) * labelSet.size + guessLabel[i]
                    model.update(goldFeature, predFeature, total, timestamp, current)
                }
            }
            println("${(iterate.toFloat() + 1) * 100 / maxIter}%")
        }
        model.average(total, timestamp, current)
        return model
    }

    fun viterbiDecode(sentence: FeaturedSequenecLabel, guessLabel: IntArray): Double {
        val labelSet = this.labelSet
        val bos = 4
        val sentenceLength = sentence.size
        val labelSize = labelSet.size

        val preMatrix = Array(sentenceLength) { IntArray(labelSize) }
        val scoreMatrix = Array(2) { DoubleArray(labelSize) }

        for (i in 0 until sentenceLength) {

            val _i = i and 1//偶数得0 奇数得1
            val _i_1 = 1 - _i//偶数得1 奇数得0
            val allFeature = sentence.featureMatrix[i]
            val transitionFeatureIndex = allFeature.size - 1
            if (0 == i) {
                allFeature[transitionFeatureIndex] = bos//一定是4
                for (j in labelSet.indices) {
                    preMatrix[0][j] = j

                    val score = score(allFeature, j)

                    scoreMatrix[0][j] = score
                }
            } else {
                for (curLabel in labelSet.indices) {

                    var maxScore = Integer.MIN_VALUE.toDouble()

                    for (preLabel in labelSet.indices) {

                        allFeature[transitionFeatureIndex] = preLabel
                        val score = score(allFeature, curLabel)

                        val curScore = scoreMatrix[_i_1][preLabel] + score

                        if (maxScore < curScore) {
                            maxScore = curScore
                            preMatrix[i][curLabel] = preLabel
                            scoreMatrix[_i][curLabel] = maxScore
                        }
                    }
                }

            }
        }

        var maxIndex = 0
        var maxScore = scoreMatrix[sentenceLength - 1 and 1][0]

        for (index in 1 until labelSet.size) {
            if (maxScore < scoreMatrix[sentenceLength - 1 and 1][index]) {
                maxIndex = index
                maxScore = scoreMatrix[sentenceLength - 1 and 1][index]
            }
        }

        for (i in sentenceLength - 1 downTo 0) {
            guessLabel[i] = labelSet[maxIndex]
            maxIndex = preMatrix[i][maxIndex]
        }

        return maxScore
    }

    fun score(featureVector: IntArray, currentTag: Int): Double {
        var score = 0.0
        for (index in featureVector) {
            if (index == -1) {
                continue
            } else if (index < -1 || index >= featureMap.featureSize()) {
                throw IllegalArgumentException("在打分时传入了非法的下标")
            } else {
                val temp = index * featureMap.tagSize() + currentTag
                score += model.parameter[temp].toDouble()    // 其实就是特征权重的累加
            }
        }
        return score
    }


}

class TrainerBuilder<T>(val data: MutableIterable<SequenceLabel<T>>, private val extractor: FeatureExtractor<T>, private val tagSet: IntArray, vararg maxIter: Int) {
    private var iter: Int

    init {
        iter = maxIter[0]
        if (iter == 0) iter = 10
    }

    fun buildDefaultModel(): StructuredPerceptronTrainer<T> {
        return StructuredPerceptronTrainer(data, extractor, tagSet, iter)
    }

//    fun build
}

class FeaturedSequenecLabel(val featureMatrix: List<IntArray>, val label: IntArray) {
    init {
        assert(featureMatrix.size == label.size)
    }
    val size = featureMatrix.size
}


fun readConllFile(directory: String): MutableIterable<SequenceLabel<Char>> {
    val list = ArrayList<SequenceLabel<Char>>()
    val root = File(directory)
    root.walk().forEach { file ->
        if (file.name.contains("gold_conll")) {
            val sentence = ArrayList<String>()
            file.bufferedReader().lineSequence().drop(1).forEach {
                try {
                    if (it != "") {
                        val words = it.split("\\s+".toRegex())[3]
                        sentence.add(words)
                    } else {
                        list.add(aaa(sentence))
                        sentence.clear()
                    }
                } catch (e: Exception) {
                    if (it.indexOf("#") != 0)
                        println(e)
                }
            }
        }
    }
    return list
}

fun readPukFile(directory: String):MutableIterable<SequenceLabel<Char>>{
    val list = ArrayList<SequenceLabel<Char>>()
    val root = File(directory)
    root.bufferedReader().lineSequence().forEach {
        if (it != ""){
            val t = aaa(bb(it))
            list.add(t)
        }
    }

    return list
}

fun bb(param: String): List<String>{
//    val pattern = Pattern.compile("(\\[(([^\\s]+/[0-9a-zA-Z]+)\\s+)+?([^\\s]+/[0-9a-zA-Z]+)]/?[0-9a-zA-Z]+)|([^\\s]+/[0-9a-zA-Z]+)")
//    val matcher = pattern.matcher(param)
    val temp = param.split("\\s+".toRegex())

    val wordList = LinkedList<String>()
    for (i in 1 until temp.size){
        val t = temp[i].split("/")[0]
        if (t ==  "") continue
        wordList.add(t)
    }
    if (wordList.isEmpty())
    // 按照无词性来解析
    {
        for (w in param.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            wordList.add(w)
        }
    }
    return wordList
}

fun aaa(sentence: List<String>): SequenceLabel<Char> {
    val sequence = ArrayList<Char>()
    val label = ArrayList<Int>()
    sentence.forEach { word ->
        sequence.addAll(word.toCharArray().asIterable())
        if (word.length == 1) {
            label.add(3)
        } else {
            label.add(0)
            label.addAll(IntArray(word.length - 2) { 1 }.asIterable())
            label.add(2)
        }
    }
    return SequenceLabel(sequence.toTypedArray(), label.toIntArray())
}