//package com.mayabot.nlp.perceptron.solution.cws
//
//import com.mayabot.nlp.perceptron.*
//import com.mayabot.nlp.utils.CharNormUtils
//import java.io.File
//import java.util.function.Consumer
//
///**
// * 用B M E S进行分词的感知机模型
// */
//class CWSPerceptron(val model: PerceptronModel) {
//
//
//    fun save(file: File) {
//        model.save(file)
//    }
//
//    fun decodeToWords(sentence: String): String {
//        val decode = decode(sentence.toCharArray())
//        val out = StringBuilder()
//        for (i in 0 until decode.size) {
//            val f = decode[i]
//            out.append(sentence[i])
//            if (f == 3 || f == 2) {
//                out.append(" | ")
//            }
//        }
//        return out.toString()
//    }
//
//    fun decode(sentence: CharArray): IntArray {
//        val featureList = ArrayList<IntArray>(sentence.size)
//        for (i in 0 until sentence.size) {
//            featureList += extractFeature(sentence, sentence.size, i, model.featureSet())
//        }
//        return model.decode(featureList)
//    }
//
//    companion object {
//
//        @JvmStatic
//        val tagList = listOf("B", "M", "E", "S")
//
//        private const val CHAR_BEGIN = '\u0001'
//
//        private const val CHAR_END = '\u0002'
//
//        fun extractFeature(sentence: CharArray, size: Int, position: Int, features: FeatureSet): IntArray {
//            val result = mutableListOf<Int>()
//            extractFeature(sentence, size, position, Consumer { f ->
//                val id = features.featureId(f)
//                if (id >= 0) {
//                    result.add(id)
//                }
//            })
//
//            //// 最后一列留给转移特征
//            result.add(0)
//
//            return result.toIntArray()
//        }
//
//        fun extractFeature(sentence: CharArray, size: Int, position: Int, callBack: Consumer<String>) {
//            val pre2Char = if (position >= 2) sentence[position - 2] else CHAR_BEGIN
//            val preChar = if (position >= 1) sentence[position - 1] else CHAR_BEGIN
//            val curChar = sentence[position]
//            val nextChar = if (position < size - 1) sentence[position + 1] else CHAR_END
//            val next2Char = if (position < size - 2) sentence[position + 2] else CHAR_END
//
//            callBack.accept(pre2Char + "1")
//            callBack.accept(curChar + "2")
//            callBack.accept(nextChar + "3")
//
//            callBack.accept(pre2Char + "/" + preChar + "4")
//            callBack.accept(preChar + "/" + curChar + "5")
//            callBack.accept(curChar + "/" + nextChar + "6")
//            callBack.accept(nextChar + "/" + next2Char + "7")
//        }
//    }
//}
//
//
///**
// * 分词感知机的训练
// */
//class CWSPerceptronTrainer(val workDir:File) {
//
//    lateinit var featureSet: FeatureSet
//
//    fun train(dir: File): CWSPerceptron {
//
//        val featureSetFile = File(workDir, "fs.dat")
//        if (featureSetFile.exists()) {
//            featureSet = FeatureSet.load(featureSetFile)
//        } else {
//            prepareFeatureSet(dir)
//            featureSet.save(featureSetFile)
//        }
//
//        println("Feature Set Size ${featureSet.size()}")
//
//        //统计有多少样本
//        var sampleSize = 0
//
//        dir.walkTopDown()
//                .filter { it.isFile && !it.name.startsWith(".") }
//                .forEach { file ->
//                    file.useLines { it.forEach { if (it.isNotBlank()) sampleSize++ } }
//                }
//
//        println("Sample Size $sampleSize")
//
//        //预先分配好空间
//        val sampleList = ArrayList<TrainSample>(sampleSize + 10)
//
//        // 解析语料库为数字化TrainSample
//        dir.walkTopDown()
//                .filter { it.isFile && !it.name.startsWith(".") }
//                .flatMap { it.readLines().filter { it.isNotBlank() }.asSequence() }
//                .forEach {
//                    sampleList += sentenceToSample(it.trim())
//                }
//
//        println("Sample List Prepare complete")
//
//        val trainer = PerceptronTrainer(featureSet, CWSPerceptron.tagList.size,sampleList, EvaluateRunner {  },
//                30)
//
//        return CWSPerceptron(trainer.train())
//
//    }
//
//    /**
//     * 把一个句子，变化为TrainSample
//     * 一个用空格分隔的句子.
//     *
//     */
//    fun sentenceToSample(line: String): TrainSample {
//        val juzi = CharArray(line.length)
//        val split = BooleanArray(line.length)
//        var len = 0
//        line.forEach { c ->
//            if (c != ' ') {
//                juzi[len++] = c
//            } else {
//                split[len - 1] = true
//            }
//        }
//        split[len - 1] = true
//
//        val list = mutableListOf<IntArray>()
//        val tagList = IntArray(len)
//
//        var from = 0
//        for (i in 0 until len) {
//            val vec = CWSPerceptron.extractFeature(juzi, len, i, featureSet)
//            list.add(vec)
//
//            if (split[i]) {
//                val wordLen = i - from + 1
//                if (wordLen == 1) {
//                    tagList[i] = 3 //S
//                } else {
//                    tagList[from] = 0 //B
//
//                    if (wordLen >= 3) {
//                        for (x in from + 1 until i) {
//                            tagList[x] = 1//M
//                        }
//                    }
//
//                    tagList[i] = 2//E
//                }
//                from = i + 1
//            }
//        }
//        return TrainSample(list, tagList)
//    }
//
//    /**
//     * 制作FeatureSet。
//     * 扫描所有语料库，为每一个特征进行编码
//     */
//    fun prepareFeatureSet(dir: File) {
//        println("开始构建FeatureSet")
//        val t1 = System.currentTimeMillis()
//        val builder = FeatureSetBuilder()
//        val fit = Consumer<String> { f ->
//            builder.put(f)
//        }
//        dir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }
//                .forEach { dictFile ->
//                    println(dictFile.absolutePath)
//                    val lines = dictFile.readLines()
//
//                    lines.forEach { line ->
//                        val out = CharArray(line.length)
//                        var p = 0
//                        line.forEach { c ->
//                            if (c != ' ') {
//                                out[p++] = CharNormUtils.convert(c)
//                            }
//                        }
//                        val len = p
//
//
//                        for (i in 0 until len) {
//                            CWSPerceptron.extractFeature(out, len, i, fit)
//                        }
//                    }
//                }
//
//        println("build DAT")
//        featureSet = builder.build()
//
//        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1}ms")
//        println("FeatureSet Size ${featureSet.size()}")
//    }
//
//}