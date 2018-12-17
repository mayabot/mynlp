package com.mayabot.nlp.segment.perceptron

import com.google.common.collect.Lists
import com.mayabot.nlp.perceptron.PerceptronModel
import java.io.File


object NRPerceptronTest {

    @JvmStatic
    fun main(args: Array<String>) {
        // train()
        test()
    }

    fun train() {
        val trainer = NRPerceptronTrainer()

        val trainFile = File("data.work/nercorpus")
        val evaluateFile = File("data.work/nercorpus.test/hr_0.txt")
//336

        // 74 这个轮次比较满意
        val cws = trainer.train(
                trainFile,
                evaluateFile,
                74, 1)

        cws.save(File("data.work/nr"))

        println("compress")
        cws.compress(0.2, 1e-4)

        cws.save(File("data.work/nr2"))

        println("After compress ...")
        NREvaluate.evaluate(evaluateFile.readLines().map { NREvaluate.text2EvaluateSample(it) }, cws)

    }

    fun test() {
        val model = PerceptronModel.load(File("data.work/nr2"))

        model.compress(0.1, 0.00001)

        val cws = PersonNamePerceptron(model)

        cws.save(File("data.work/nr-2"))
////
//        val tester = File("data/corpus.segment/backoff2005/msr_training.txt").readLines()
//
//        CWSEvaluate.evaluate(tester, cws)
////        println("---")
        val evaluateFile = File("data.work/nercorpus.test/hr_0.txt")
        NREvaluate.evaluate(evaluateFile.readLines().map { NREvaluate.text2EvaluateSample(it) }, cws)
////

        val text =
        //"YouTube针对用户发表声明称:“感谢你们报告YouTube、YouTube TV和YouTube Music无法访问的问题。我们正在努力解决这个问题，一旦修好，我们会通知你们。对于由此造成的不便，我们深表歉意，并将继续保持更新。”\n" +
//                "美国哈佛大学医学院近日宣布，曾在该机构任职的皮耶罗·安韦萨有31篇论文因造假需要撤稿。这一消息震惊全球学术界，因为安韦萨曾被认为开创了心肌细胞再生的新领域，已经享誉10多年。他在心肌上动的“心机”终被揭穿，警示科研人员靠造假可能一时得意，但不可能永远欺骗所有人。\n" +
//                        "这个是李国金的快递\n" +
                        Lists.newArrayList(
                                "早就看出了肖克利公司免不了要破产的结局",
                                "服务站和江阴顺天村项目",
                                "杜绝超生",
                                "生前杜绝超生",
                                "龚学平等领导说,邓颖超生前杜绝超生",
                                "中共中央总书记,国家主席江泽民",
                                "李鹏又来到工厂退休职工郭树范和闫戌麟家看望慰问",
                                "沈沈先生夫妇抱着3个月的儿子来湛江向梁惠珍表示感谢",
                                "袁庄村民李某酒后滋事",
                                "签约仪式前，秦光荣、李纪恒、仇和等一同会见了参加签约的企业家。",
                                "武大靖创世界纪录夺冠，中国代表团平昌首金",
                                "区长庄木弟新年致辞",
                                "朱立伦：两岸都希望共创双赢 习朱历史会晤在即",
                                "陕西首富吴一坚被带走 与令计划妻子有交集",
                                "据美国之音电台网站4月28日报道，8岁的凯瑟琳·克罗尔（凤甫娟）和很多华裔美国小朋友一样，小小年纪就开始学小提琴了。她的妈妈是位虎妈么？",
                                "凯瑟琳和露西（庐瑞媛），跟她们的哥哥们有一些不同。",
                                "王国强、高峰、汪洋、张朝阳光着头、韩寒、小四、仇和",
                                "张浩和胡健康复员回家了、一个为元",
                                "王总和小丽结婚了",
                                "报道称，韩国瑜之所以对陈水扁的言论感到如此愤怒",
                                "编剧邵钧林和稽道青说陈汝烨是个好演员",
                                "这里有关天培的有关、事迹、",
                                "这是李国金的快递",
                                "记者 李国金 陈汝烨报道",
                                "而剧中让人尤其印象深刻的当属滑稽戏名家王汝刚演出的老太王翠花。在研讨会上，众多专家都表示，王汝刚跨性别扮演老太太的表演功夫已经炉火纯青，可谓上海滑稽界一绝，甚至称得上“上海舞台上第一老太婆”。著名影视编剧王丽萍第一次在剧场完整观看滑稽戏，她也对王汝刚的表演十分惊叹，说自己差点在舞台上认不出哪一个是王汝刚。\n" +
                                        "事实上，王汝刚也是新成立的上海独脚戏传承艺术中心的党支部书记兼主任。他表示，原有两个滑稽剧团现有人员合起来也并不是很多，对于当下的滑稽界而言，培养青年演员是迫不及待的任务。《舌尖上的诱惑》作为传承中心推出的首部大戏，既是希望能够有社会影响力，也希望能够给剧团演员更多的展示机会。而这部剧的题材贴近民生，贴近百姓，一经问世就市场反响十分看好。\n" +
                                        "上海滑稽剧团凌梅芳在研讨会上对人滑和青滑合并表示了祝贺，她说，优质资源整合以后，剧团各方面的实力都增强了。在舞台上我们也看到了很多原来两个团的主要的力量，现在全都集中在一起，优势互补。"
                                , "先后视察了华鑫海欣楼宇党建（群团）服务站和江阴顺天村项目"
                        ).joinToString(separator = "\n")

//        val text = "心肌细胞是心脏泵血的动力来源"
//
//        cws.learn("拆迁人 为")
//        cws.learn("外地人 生 孩子")
//        cws.learn("外地人 生 孩子")
//        cws.learn("外地人 生 孩子")
//        cws.learn("小孩 多大 学 跳舞")
//        cws.learn("仇和 等")
//        cws.learn("区 长 庄木弟 新 年 致 辞")
//        cws.learn("与 令计划 妻子")
//        cws.learn("与 令计划 妻子")
//        cws.learn("与 令计划 妻子")
//        cws.learn("与 令计划 妻子")
//        cws.learn("与 令计划")
//        cws.learn("与 令计划")

        text.lines().forEach {
            println(it + " \n" + cws.findPersonName(it.toCharArray()).joinToString(separator = " / "))
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
//    val cws = CWSPerceptron(PerceptronModel.load(File("data/pcws/main.model")))
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