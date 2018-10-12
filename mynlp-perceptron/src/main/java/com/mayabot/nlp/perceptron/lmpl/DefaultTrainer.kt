package com.mayabot.nlp.perceptron.lmpl

import com.google.common.io.Resources
import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.perceptron.dataReader.PkuFileReader
import com.mayabot.nlp.perceptron.extractor.DefaultPosTagFeatureExtractor
import com.mayabot.nlp.perceptron.extractor.DefaultWordSplitFeatureExtractor
import com.mayabot.nlp.perceptron.model.CostumisedPerceptron
import java.io.File
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

//abstract class DefaultParser<E, T, P>(val featureNumber: Int) : PerceptronDataParser<T, E> where E : Enum<E>, P : FeatureExtractor<T> {
//    abstract val featureMap: FeatureMap<String>
//    abstract val parameter: IntArray
//    abstract val featureExtractor: P
//    override fun parse(source: MutableIterable<SequenceLabel<T>>?): PerceptronTrainer<T> {
//        source!!.forEach {
//            for (i in 0 until it.length()) {
////                featureExtractor.featureExtract(it.sequence,i)
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
    val reader = PkuFileReader("/Users/mei_chaofeng/conll/conll-2012/v4/data/train/data/chinese/199801.txt")
//    val list = conllFileReader("/Users/mei_chaofeng/conll/conll-2012/v4/data/train/data/chinese/annotations")
//    val list = pkuFileReader("/Users/mei_chaofeng/conll/conll-2012/v4/data/train/data/chinese/199801.txt")

    val list = reader.readWord()
    val builder = TrainerBuilder(list, DefaultWordSplitFeatureExtractor(), listOf<String>("B", "M", "E", "S"), 5)
    val trainer = builder.buildDefaultModel()
    val wordModel = trainer.train()
    val resultSet = ArrayList<String>()

    val tagSet = ArrayList<String>()
    val list2 = reader.readPosTag(tagSet)
    val builder2 = TrainerBuilder(list2, DefaultPosTagFeatureExtractor(), tagSet, 5)
    val trainer2 = builder2.buildDefaultModel()
    val posModel = trainer2.train()

//    val test = "上海万行信息科技有限公司提供给用户全新的中文自然语言处理技术,产品和方法,我们愿望在中国的不同领域和行业的用户在使用我们的行业解决方案后,为他们创造商业价值。"
    val test = "解决方案和服务必须到位。"
    val resultSplit = wordModel.decode(test.toCharArray().toTypedArray())
    println(resultSplit)
//    println(posModel.decode2(resultSplit.split("\\s+".toRegex()).toTypedArray(), tagSet))

//    println(posModel.decode())

//    model.compress(0.8)
//    resultSet.add(model.decode("我爱北京天安门和长城".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("还这个世界一片蓝天".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("小明想要一个女朋友".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("我们去炮台湾公园玩吧".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("老刘兴高采烈地来上班了".toCharArray().toTypedArray()))
//    resultSet.add(model.decode("下雨天地面积水".toCharArray().toTypedArray()))
//
//    resultSet.forEach {
//        println(it)
//    }
//    resultSet.clear()
//    println()
//    println()
//    println()
//
//    val l= ArrayList<Int>()
//    l.add(0)
//    l.add(1)
//    l.add(2)
//    l.add(0)
//    l.add(2)
//    l.add(0)
//    l.add(2)
//    val string = "下雨天地面积水"
//    val c = string.toCharArray().toTypedArray()
//    val extractor = DefaultWordSplitFeatureExtractor()
//    val sequence =      SequenceLabel(c, l.toIntArray())
//    val temp = sequence.sequence.mapIndexed { index, _ ->
//        extractor.extractFeature(sequence.sequence, index, model.featureMap)
//    }
//    model.update(FeaturedSequenceLabel(temp, sequence.label))
//
//    resultSet.add(model.decode("下雨天地面积水".toCharArray().toTypedArray()))
//    resultSet.forEach {
//        println(it)
//    }

}

class StructuredPerceptronTrainer<T>(dataInput: MutableIterable<SequenceLabel<T>>,
                                     private val featureExtractor: FeatureExtractor<T>,
                                     tagSet:List<String>,
                                     val maxIter: Int
) {
    private var dataSet: List<FeaturedSequenceLabel>
    private var featureMap: FeatureMap = FeatureMap(tagSet)

    init {
        dataSet = dataInput.map {
            //sentence
            val sequence = it.sequence
            val temp = sequence.mapIndexed { index, _ ->
                featureExtractor.extractFeature(sequence, index, featureMap)
            }
            FeaturedSequenceLabel(temp, it.label)
        }
        println("feature extraction completed")
    }

    fun train(): CostumisedPerceptron<T> {
        val model = CostumisedPerceptron(
                featureMap,
                FloatArray(featureMap.featureSize() * featureMap.tagSize()),
                featureExtractor
        )
        //应该是权重的总和 最后要平均？
        val total = DoubleArray(model.parameter.size)
        //时间戳 每个正确预测的存活时间
        val timestamp = IntArray(model.parameter.size)
        var current = 0//第N次更新
        val size = dataSet.size
        for (iterate in 0 until maxIter) {
            dataSet.forEach {
                current++
                model.update(it,total,timestamp,current)
            }
            println("${(iterate.toFloat() + 1) * 100 / maxIter}%")
        }
        model.average(total, timestamp, current)
        return model
    }

    fun train(threadNumber: Int): CostumisedPerceptron<T> {
        val part = threadNumber + 1
        val size = featureMap.featureSize() * featureMap.tagSize()
        val modelArray = Array(part) {
            CostumisedPerceptron(featureMap, FloatArray(size), featureExtractor)
        }
        val division = dataSet.size / part
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        var countDownLatch = CountDownLatch(part)
        for (s in 0 until (threadNumber + 1)) {
            executor.submit {
                try {
                    if (s == part - 1) {
                        for (i in (part - 1) * division until dataSet.size) {
                            modelArray[s].update(dataSet[i])
                        }
                    } else {
                        for (i in s * division until (s + 1) * division) {
                            modelArray[s].update(dataSet[i])
                        }
                    }
                } finally {
                    countDownLatch.countDown()
                }
            }
        }
        countDownLatch.await()
        executor.shutdownNow()

        val result = FloatArray(size) { 0f }
        for (i in 0 until size) {
            for (j in 0 until part) {
                result[i] = modelArray[j].parameter[i]
            }
            result[i] = result[i] / part
        }

        return CostumisedPerceptron(
                featureMap,
                result,
                featureExtractor
        )

    }
}

class TrainerBuilder<T>(private val data: MutableIterable<SequenceLabel<T>>, private val extractor: FeatureExtractor<T>, private val tagSet: List<String>, vararg maxIter: Int) {
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

class FeaturedSequenceLabel(val featureMatrix: List<IntArray>, val label: IntArray) {
    val size = featureMatrix.size
}




fun pkuSplitter(param: String): List<String> {
    val temp = param.split("\\s+".toRegex())
    val wordList = LinkedList<String>()
    for (i in 1 until temp.size) {
        var t = temp[i].split("/")[0]
        if (t == "") continue
        if (t.indexOf("[") != -1) t = t.substring(1)
        wordList.add(t)
    }
    return wordList
}

fun listTOSequence(sentence: List<String>): SequenceLabel<Char> {
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
//    val normed = CharNorm.convert(sentence.reduce { acc, s -> acc + s }).toTypedArray()
    return SequenceLabel(sequence.toTypedArray(), label.toIntArray())
}

object CharNorm {
    val normTable = CharArray(65535)

    init {
        val readLines = Resources.asCharSource(Resources.getResource("norm"), Charsets.UTF_8).readLines()
        readLines.forEach { line ->
            val first = line[0]
            val to = line[2]
            normTable[first.toInt()] = to
        }
    }

    fun convert(input: Char): Char {
        if (input != '\u0000')
            return normTable[input.toInt()]
        return input
    }


    fun convert(input: String): String {
        var target = input.toCharArray()

        for (i in 0 until target.size) {
            val ch = target[i].toInt()
            val tch = normTable[ch]
            if (tch != '\u0000') {
                target[i] = tch
            }
        }
        return String(target)
    }
}

//dataSet.forEach {
//                val length = it.size
//                current++
//                val guessLabel = IntArray(length)
//            viterbiDecode(it, guessLabel)
//            for (i in 0 until length) {
//                //序号代替向量 替代数组
//                val featureVector = it.featureMatrix[i]
//                //正确标签
//                val goldFeature = IntArray(featureVector.size)
//                //预测标签
//                val predFeature = IntArray(featureVector.size)
//                for (j in 0 until featureVector.size - 1) {
//                    //特征 -> 对应标签
//                    goldFeature[j] = featureVector[j] * labelSet.size + it.label[i]
//                    predFeature[j] = featureVector[j] * labelSet.size + guessLabel[i]
//                }
//                goldFeature[featureVector.size - 1] = (if (i == 0) labelSet.size else it.label[i - 1]) * labelSet.size + it.label[i]
//                predFeature[featureVector.size - 1] = (if (i == 0) labelSet.size else guessLabel[i - 1]) * labelSet.size + guessLabel[i]
//                val cha = model.update(goldFeature, predFeature, total, timestamp, current)
//                if (cha) asd++
//                if (iterate == 0 && cha) ssd++
//                ac = ac || cha
//            }