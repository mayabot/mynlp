package com.mayabot.nlp.cli

import com.mayabot.nlp.perceptron.solution.ner.NERPerceptronTrainer
import com.mayabot.nlp.segment.perceptron.POSPerceptronTrainer
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.File

fun main(args: Array<String>) {
    POSPerceptronTrain().run(" -f pku -o xx -e 10  -t 2".split(" ").toTypedArray())
}


class NerPerceptronTrain : CmdRunner {

    override fun run(args: Array<out String>?) {
        val line = DefaultParser().parse(options(), args)

        val file = line.getParsedOptionValue("f") as File
        val evFile = line.getParsedOptionValue("ev") as File
        val outFile = (line.getParsedOptionValue("o") ?: File("ner-model")) as File

        val iter = (line.getParsedOptionValue("e") as Number?)?.toInt() ?: 1
        val threadNumber = (line.getParsedOptionValue("t") as Number?)?.toInt() ?: 1

        println("outFile $outFile")
        println("Iter $iter")
        println("threadNum $threadNumber")

        val model = NERPerceptronTrainer().train(file, evFile, iter, threadNumber)
        model.save(outFile)
    }

    override fun usage() = "pos-train"

    override fun options(): Options {
        var options = Options()

        options.addOption(Option.builder("f")
                .longOpt("file")
                .desc("被分词的文本文件")
                .argName("Train File")
                .type(File::class.java)
                .hasArgs()
                .required()
                .build())

        options.addOption(Option.builder("o")
                .longOpt("out")
                .desc("被分词的文本文件")
                .argName("Save File dir")
                .type(File::class.java)
                .hasArgs()
                .build())

        options.addOption(Option.builder("ev")
                .longOpt("evaluate")
                .desc("评估文件")
                .argName("evaluate file")
                .type(File::class.java)
                .hasArgs()
                .required()
                .build())

        options.addOption(Option.builder("e")
                .longOpt("epoll")
                .desc("迭代次数")
                .argName("iter count")
                .type(Number::class.java)
                .hasArgs()
                .build())

        options.addOption(Option.builder("t")
                .longOpt("thread")
                .desc("线程数")
                .argName("thread num")
                .type(Number::class.java)
                .hasArgs()
                .build())
        return options
    }
}

class POSPerceptronTrain : CmdRunner {

    override fun run(args: Array<out String>?) {
        val line = DefaultParser().parse(options(), args)

        val file = line.getParsedOptionValue("f") as File
        val outFile = (line.getParsedOptionValue("o") ?: File("pos-model")) as File

        val iter = (line.getParsedOptionValue("e") as Number?)?.toInt() ?: 1
        val threadNumber = (line.getParsedOptionValue("t") as Number?)?.toInt() ?: 1

        println("outFile $outFile")
        println("Iter $iter")
        println("threadNum $threadNumber")

        val model = POSPerceptronTrainer().train(file, iter, threadNumber)
        model.save(outFile)
    }

    override fun usage() = "pos-train"

    override fun options(): Options {
        var options = Options()

        options.addOption(Option.builder("f")
                .longOpt("file")
                .desc("被分词的文本文件")
                .argName("Train File")
                .type(File::class.java)
                .hasArgs()
                .required()
                .build())

        options.addOption(Option.builder("o")
                .longOpt("out")
                .desc("被分词的文本文件")
                .argName("Save File dir")
                .type(File::class.java)
                .hasArgs()
                .build())

        options.addOption(Option.builder("e")
                .longOpt("epoll")
                .desc("迭代次数")
                .argName("iter count")
                .type(Number::class.java)
                .hasArgs()
                .build())

        options.addOption(Option.builder("t")
                .longOpt("thread")
                .desc("线程数")
                .argName("thread num")
                .type(Number::class.java)
                .hasArgs()
                .build())
        return options
    }
}