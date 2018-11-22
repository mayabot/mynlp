import com.mayabot.nlp.perceptron.solution.cws.CWSEvaluate
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptron
import com.mayabot.nlp.perceptron.solution.cws.CWSPerceptronTrainer
import java.io.File


object CWSPerceptronTest {

    @JvmStatic
    fun main(args: Array<String>) {
        train()
//        test()
    }

    fun train() {
        val trainer = CWSPerceptronTrainer()

        val trainFile = File("data/corpus.segment/backoff2005/msr_training.txt")
        val evaluateFile = File("data/corpus.segment/backoff2005/msr_test_gold.txt")

        val cws = trainer.train(
                trainFile,
                evaluateFile,
                15, 2)

        println("compress")
        cws.compress(0.2, 1e-3)

        cws.save(File("data/pcws/model"))

        println("After compress ...")
        CWSEvaluate.evaluate(evaluateFile.readLines(), cws)

    }

    fun test() {
        val cws = CWSPerceptron.load(File("data/pcws/model"))

        val tester = File("data/corpus.segment/backoff2005/msr_training.txt").readLines()

        CWSEvaluate.evaluate(tester, cws)
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
                "这是你买的第几套房子，这就是总统套房了，芝士汉堡是什么味道\n" +
                "小孩多大学跳舞\n" +
                "外地人生孩子\n" +
                "被拆迁人为低收入"
        ""
        text.lines().forEach {
            println(it + " \n" + cws.decodeToWordList(it).joinToString(separator = " / "))
        }

    }
}

//
//fun main2(args: Array<String>) {
////    val trainer = CWSPerceptronTrainer()
////
////    val cws = trainer.train(File("data/corpus.segment"))
////
////    cws.save(File("data/pcws/main.model"))
//
//    val cws = CWSPerceptron(Perceptron.load(File("data/pcws/main.model")))
//
//    println("---")
//
//    val text = "" +
//            "南海地区的航行和飞越自由根本不存在问题，每年10万余艘各国各类船只能够在南海安全、顺利地通行，而中国南沙和西沙群岛远离国际航道。但在美方眼里这不叫航行自由，美方所谓“航行自由行动”，是按照自己对国际法的单方面解释，派出军舰挑战其它国家对海上领土主权和海洋权益的所谓“过度”主张和行使。放着宽阔的南海国际航道不走，美国军舰却屡次进入中国驻守的南海岛礁12海里之内宣示“航行自由”，实际上是别有用心。\n" +
//            "美国当地时间星期二，谷歌旗下流媒体视频网站YouTube在晚上6点左右陷入全球性宕机状态，直到7点20分才恢复功能。\n" +
//            "\n" +
//            "YouTube针对用户发表声明称:“感谢你们报告YouTube、YouTube TV和YouTube Music无法访问的问题。我们正在努力解决这个问题，一旦修好，我们会通知你们。对于由此造成的不便，我们深表歉意，并将继续保持更新。”\n" +
//            "美国哈佛大学医学院近日宣布，曾在该机构任职的皮耶罗·安韦萨有31篇论文因造假需要撤稿。这一消息震惊全球学术界，因为安韦萨曾被认为开创了心肌细胞再生的新领域，已经享誉10多年。他在心肌上动的“心机”终被揭穿，警示科研人员靠造假可能一时得意，但不可能永远欺骗所有人。\n" +
//            "\n" +
//            "心肌上动“心机”\n" +
//            "\n" +
//            "心肌细胞是心脏泵血的动力来源，心肌细胞出问题可能会导致严重疾病甚至死亡。因此，如果能让心脏中长出新的心肌细胞，替换掉有问题的细胞，以此修复心脏，无疑是医学上的一大突破。\n" +
//            "安韦萨就在心肌上动起了“心机”。2001年，他还在纽约医学院工作时，在英国学术刊物《自然》上发表一篇论文，说可以用来自骨髓的c-kit干细胞使心肌再生。随后，他又于2003年在美国《细胞》杂志发文说不需要骨髓干细胞，使用成熟的心脏干细胞就能修复心肌。有研究人员曾对他的这两项研究成果提出质疑。\n" +
//            "我要购买一个双层芝士汉堡\n" +
//            "这是你买的第几套房子，这就是总统套房了，芝士汉堡是什么味道\n" +
//            "小孩多大学跳舞\n" +
//            "外地人生孩子\n" +
//            "被拆迁人为低收入"
//    ""
//    text.lines().forEach {
//        println(it + " \n" + cws.decodeToWords(it))
//        println("")
//        println("")
//    }
//
//
//}