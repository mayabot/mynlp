package com.mayabot.nlp.segment.perceptron

import com.google.common.collect.Lists
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.segment.plugins.personname.NREvaluate
import com.mayabot.nlp.segment.plugins.personname.NRPerceptronTrainer
import com.mayabot.nlp.segment.plugins.personname.PersonNamePerceptron
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File


object NRPerceptronTest {

    @JvmStatic
    fun main(args: Array<String>) {
//         train()
//        test()
        testSpeed()
    }

    fun train() {
        val trainer = NRPerceptronTrainer()

        val trainFile = File("data.work/nr.corpus")
        val evaluateFile = File("data.work/nr.corpus.test/hr_0.txt")
//336

        // 74 这个轮次比较满意
        val cws = trainer.train(
                trainFile,
                evaluateFile,
                186, 1)

        cws.save(File("data.work/nr"))

        println("compress")
        cws.compress(0.2, 1e-4)

        cws.save(File("data.work/nr2"))

        println("After compress ...")
        NREvaluate.evaluate(evaluateFile.readLines().map { NREvaluate.text2EvaluateSample(it) }, cws)
    }

    fun testSpeed() {
        val model = PerceptronModel.load(File("data.work/nr2"))
        val cws = PersonNamePerceptron(model)

        val lines = File("data.work/hongloumeng.txt").readLines().map { CharNormUtils.convert(it).toCharArray() }

        for (line in lines) {
            cws.findPersonName(line)
        }
        for (line in lines) {
            cws.findPersonName(line)
        }

        val t1 = System.currentTimeMillis()
        for (line in lines) {
            cws.findPersonName(line)
        }
        val t2 = System.currentTimeMillis()

        println("${t2 - t1}ms")
    }

    fun test() {
        val model = PerceptronModel.load(File("data.work/nr2"))


        val cws = PersonNamePerceptron(model)

        cws.learn("与", "令计划", "妻子")

////
//        val tester = File("data/corpus.segment/backoff2005/msr_training.txt").readLines()
//
//        CWSEvaluate.evaluate(tester, cws)
////        println("---")
        val evaluateFile = File("data.work/nercorpus.test/hr_0.txt")
        NREvaluate.evaluate(evaluateFile.readLines().map { NREvaluate.text2EvaluateSample(it) }, cws)
//////

        val text =
        //"YouTube针对用户发表声明称:“感谢你们报告YouTube、YouTube TV和YouTube Music无法访问的问题。我们正在努力解决这个问题，一旦修好，我们会通知你们。对于由此造成的不便，我们深表歉意，并将继续保持更新。”\n" +
//                "美国哈佛大学医学院近日宣布，曾在该机构任职的皮耶罗·安韦萨有31篇论文因造假需要撤稿。这一消息震惊全球学术界，因为安韦萨曾被认为开创了心肌细胞再生的新领域，已经享誉10多年。他在心肌上动的“心机”终被揭穿，警示科研人员靠造假可能一时得意，但不可能永远欺骗所有人。\n" +
//                        "这个是李国金的快递\n" +
                Lists.newArrayList(
                        "早就看出了肖克利公司免不了要破产的结局",
                        "服务站和江阴顺天村项目",
                        "杜绝超生",
                        "李国金",
                        "陈汝烨",
                        "有一次孩子说要送给张贺年老师一张贺年卡",
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
                        "报道称，高雄新市长韩国瑜之所以对陈水扁的言论感到如此愤怒",
                        "编剧邵钧林和稽道青说陈汝烨是个好演员",
                        "这里有关天培的有关、事迹、",
                        "这是李国金的快递",
                        "记者 李国金 陈汝烨报道",
                        "而剧中让人尤其印象深刻的当属滑稽戏名家王汝刚演出的老太王翠花。在研讨会上，众多专家都表示，王汝刚跨性别扮演老太太的表演功夫已经炉火纯青，可谓上海滑稽界一绝，甚至称得上“上海舞台上第一老太婆”。著名影视编剧王丽萍第一次在剧场完整观看滑稽戏，她也对王汝刚的表演十分惊叹，说自己差点在舞台上认不出哪一个是王汝刚。\n" +
                                "事实上，王汝刚也是新成立的上海独脚戏传承艺术中心的党支部书记兼主任。他表示，原有两个滑稽剧团现有人员合起来也并不是很多，对于当下的滑稽界而言，培养青年演员是迫不及待的任务。《舌尖上的诱惑》作为传承中心推出的首部大戏，既是希望能够有社会影响力，也希望能够给剧团演员更多的展示机会。而这部剧的题材贴近民生，贴近百姓，一经问世就市场反响十分看好。\n" +
                                "上海滑稽剧团凌梅芳在研讨会上对人滑和青滑合并表示了祝贺，她说，优质资源整合以后，剧团各方面的实力都增强了。在舞台上我们也看到了很多原来两个团的主要的力量，现在全都集中在一起，优势互补。"
                        , "先后视察了华鑫海欣楼宇党建（群团）服务站和江阴顺天村项目"
                ).joinToString(separator = "\n")


        text.lines().forEach {
            println(it + " \n" + cws.findPersonName(it.toCharArray()).joinToString(separator = " / "))
        }


    }
}
