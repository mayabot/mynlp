package com.mayabot.nlp.perceptron.solution.cws

import com.mayabot.nlp.perceptron.*
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File
import java.util.function.Consumer

fun main(args: Array<String>) {
//    val trainer = CWSPerceptronTrainer()
//
//    val cws = trainer.train(File("data/corpus.segment"))
//
//    cws.save(File("data/pcws/main.model"))

    val cws = CWSPerceptron(CostumisedPerceptron.load(File("data/pcws/main.model")))

    println("---")

    val text = "" +
            "南海地区的航行和飞越自由根本不存在问题，每年10万余艘各国各类船只能够在南海安全、顺利地通行，而中国南沙和西沙群岛远离国际航道。但在美方眼里这不叫航行自由，美方所谓“航行自由行动”，是按照自己对国际法的单方面解释，派出军舰挑战其它国家对海上领土主权和海洋权益的所谓“过度”主张和行使。放着宽阔的南海国际航道不走，美国军舰却屡次进入中国驻守的南海岛礁12海里之内宣示“航行自由”，实际上是别有用心。\n" +
            "美国当地时间星期二，谷歌旗下流媒体视频网站YouTube在晚上6点左右陷入全球性宕机状态，直到7点20分才恢复功能。\n" +
            "\n" +
            "YouTube针对用户发表声明称:“感谢你们报告YouTube、YouTube TV和YouTube Music无法访问的问题。我们正在努力解决这个问题，一旦修好，我们会通知你们。对于由此造成的不便，我们深表歉意，并将继续保持更新。”\n" +
            "美国哈佛大学医学院近日宣布，曾在该机构任职的皮耶罗·安韦萨有31篇论文因造假需要撤稿。这一消息震惊全球学术界，因为安韦萨曾被认为开创了心肌细胞再生的新领域，已经享誉10多年。他在心肌上动的“心机”终被揭穿，警示科研人员靠造假可能一时得意，但不可能永远欺骗所有人。\n" +
            "\n" +
            "心肌上动“心机”\n" +
            "\n" +
            "心肌细胞是心脏泵血的动力来源，心肌细胞出问题可能会导致严重疾病甚至死亡。因此，如果能让心脏中长出新的心肌细胞，替换掉有问题的细胞，以此修复心脏，无疑是医学上的一大突破。\n" +
            "安韦萨就在心肌上动起了“心机”。2001年，他还在纽约医学院工作时，在英国学术刊物《自然》上发表一篇论文，说可以用来自骨髓的c-kit干细胞使心肌再生。随后，他又于2003年在美国《细胞》杂志发文说不需要骨髓干细胞，使用成熟的心脏干细胞就能修复心肌。有研究人员曾对他的这两项研究成果提出质疑。\n" +
            "我要购买一个双层芝士汉堡\n" +
            "这是你买的第几套房子，这就是总统套房了，芝士汉堡是什么味道" +
            ""
    text.lines().forEach {
        println(it + " \n" + cws.decodeToWords(it))
        println("")
        println("")
    }


}

/**
 * 用B M E S进行分词的感知机模型
 */
class CWSPerceptron(val model: PerceptronModel) {


    fun save(file: File) {
        model.save(file)
    }

    fun decodeToWords(sentence: String): String {
        val decode = decode(sentence.toCharArray())
        val out = StringBuilder()
        for (i in 0 until decode.size) {
            val f = decode[i]
            out.append(sentence[i])
            if (f == 3 || f == 2) {
                out.append(" | ")
            }
        }
        return out.toString()
    }

    fun decode(sentence: CharArray): IntArray {
        val featureList = ArrayList<IntArray>(sentence.size)
        for (i in 0 until sentence.size) {
            featureList += extractFeature(sentence, sentence.size, i, model.featureSet())
        }
        val decodeResult = model.decode(featureList)

        return decodeResult
    }

    companion object {

        @JvmStatic
        val tagList = listOf("B", "M", "E", "S")

        private const val CHAR_BEGIN = '\u0001'

        private const val CHAR_END = '\u0002'

        fun extractFeature(sentence: CharArray, size: Int, position: Int, features: FeatureSet): IntArray {
            val result = mutableListOf<Int>()
            extractFeature(sentence, size, position, Consumer { f ->
                val id = features.featureId(f)
                if (id >= 0) {
                    result.add(id)
                }
            })

            //// 最后一列留给转移特征
            result.add(0)

            return result.toIntArray()
        }

        fun extractFeature(sentence: CharArray, size: Int, position: Int, callBack: Consumer<String>) {
            val pre2Char = if (position >= 2) sentence[position - 2] else CHAR_BEGIN
            val preChar = if (position >= 1) sentence[position - 1] else CHAR_BEGIN
            val curChar = sentence[position]
            val nextChar = if (position < size - 1) sentence[position + 1] else CHAR_END
            val next2Char = if (position < size - 2) sentence[position + 2] else CHAR_END

            callBack.accept(pre2Char + "1")
            callBack.accept(curChar + "2")
            callBack.accept(nextChar + "3")

            callBack.accept(pre2Char + "/" + preChar + "4")
            callBack.accept(preChar + "/" + curChar + "5")
            callBack.accept(curChar + "/" + nextChar + "6")
            callBack.accept(nextChar + "/" + next2Char + "7")
        }
    }
}


/**
 * 分词感知机的训练
 */
class CWSPerceptronTrainer(val workDir: File = File("data/pcws")) {

    lateinit var featureSet: FeatureSet

    fun train(dir: File): CWSPerceptron {

        val featureSetFile = File(workDir, "fs.dat")
        if (featureSetFile.exists()) {
            featureSet = FeatureSet.load(featureSetFile)
        } else {
            prepareFeatureSet(dir)
            featureSet.save(featureSetFile)
        }

        println("Feature Set Size ${featureSet.size()}")

        //统计有多少样本
        var sampleSize = 0

        dir.walkTopDown()
                .filter { it.isFile && !it.name.startsWith(".") }
                .forEach { file ->
                    file.useLines { it.forEach { if (it.isNotBlank()) sampleSize++ } }
                }

        println("Sample Size $sampleSize")

        //预先分配好空间
        val sampleList = ArrayList<TrainSample>(sampleSize + 10)

        // 解析语料库为数字化TrainSample
        dir.walkTopDown()
                .filter { it.isFile && !it.name.startsWith(".") }
                .flatMap { it.readLines().filter { it.isNotBlank() }.asSequence() }
                .forEach {
                    sampleList += sentenceToSample(it.trim())
                }

        println("Sample List Prepare complete")

        val trainer = SPTrainer(featureSet, CWSPerceptron.tagList.size)

        return CWSPerceptron(trainer.train(sampleList, 30))

    }

    /**
     * 一个用空格分隔的句子
     */
    fun sentenceToSample(line: String): TrainSample {
        val juzi = CharArray(line.length)
        val split = BooleanArray(line.length)
        var len = 0
        line.forEach { c ->
            if (c != ' ') {
                juzi[len++] = c
            } else {
                split[len - 1] = true
            }
        }
        split[len - 1] = true

        val list = mutableListOf<IntArray>()
        val tagList = IntArray(len)

        var from = 0
        for (i in 0 until len) {
            val vec = CWSPerceptron.extractFeature(juzi, len, i, featureSet)
            list.add(vec)

            if (split[i]) {
                val wordLen = i - from + 1
                if (wordLen == 1) {
                    tagList[i] = 3 //S
                } else {
                    tagList[from] = 0 //B

                    if (wordLen >= 3) {
                        for (x in from + 1 until i) {
                            tagList[x] = 1//M
                        }
                    }

                    tagList[i] = 2//E
                }
                from = i + 1
            }
        }
        return TrainSample(list, tagList)
    }

    /**
     * 制作FeatureSet。
     * 扫描所有语料库，为每一个特征进行编码
     */
    fun prepareFeatureSet(dir: File) {
        println("开始构建FeatureSet")
        val t1 = System.currentTimeMillis()
        val builder = FeatureSetBuilder()
        val fit = Consumer<String> { f ->
            builder.put(f)
        }
        dir.walkTopDown().filter { it.isFile && !it.name.startsWith(".") }
                .forEach { dictFile ->
                    println(dictFile.absolutePath)
                    val lines = dictFile.readLines()

                    lines.forEach { line ->
                        val out = CharArray(line.length)
                        var p = 0
                        line.forEach { c ->
                            if (c != ' ') {
                                out[p++] = CharNormUtils.convert(c)
                            }
                        }
                        val len = p


                        for (i in 0 until len) {
                            CWSPerceptron.extractFeature(out, len, i, fit)
                        }
                    }
                }

        println("build DAT")
        featureSet = builder.build()

        println("FeatureSet构建完成,用时${System.currentTimeMillis() - t1}ms")
        println("FeatureSet Size ${featureSet.size()}")
    }

}