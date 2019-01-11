package com.mayabot.nlp.cli

import com.mayabot.nlp.segment.cws.CWSEvaluate
import com.mayabot.nlp.segment.cws.CWSPerceptronTrainer
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.File


fun main(args: Array<String>) {
    //PerceptronTrain().run(" -f pku -o xx -e 10  -t 2".split(" ").toTypedArray())
    PerceptronTrain().run(" -t cws -i data -o xx -e 10 -n 2".split(" ").toTypedArray())
}


/**
 * ./bin/mynlp-cli train -t cws -i data/cws/ -o cws-model -eva data/cws/pku/199801.txt -e 150 -n 1  >  out.cws.file  2>&1  &
 */
class PerceptronTrain : CmdRunner {

    override fun run(args: Array<out String>?) {
        val line = DefaultParser().parse(options(), args)

        val type = line.getParsedOptionValue("t") as String
        val file = line.getParsedOptionValue("i") as File
        val outFile = line.getParsedOptionValue("o") as File
        val evaFile = line.getParsedOptionValue("eva") as File
        val iter = (line.getParsedOptionValue("e") as Number?)?.toInt() ?: 1
        val threadNumber = (line.getParsedOptionValue("n") as Number?)?.toInt() ?: 1

        println("Type $type")
        println("Input $file")
        println("outFile $outFile")
        println("Iter $iter")
        println("threadNum $threadNumber")

        when (type) {
            "cws" -> {
                val trainer = CWSPerceptronTrainer()

                val model = trainer.train(
                        file,
                        evaFile,
                        iter, threadNumber)

                println("compress .... ")
                model.compress(0.2, 1e-3)

                println("After compress ...")
                CWSEvaluate.evaluate(evaFile.readLines(), model)

                model.save(outFile)
            }

            "pos" -> {

            }
        }


    }

    override fun usage() = "pos-train"

    override fun options(): Options {
        var options = Options()

        options.addOption(Option.builder("t")
                .longOpt("type")
                .desc("训练类型cws pos ner name")
                .argName("Train File")
                .type(String::class.java)
                .hasArgs()
                .required()
                .build())

        options.addOption(Option.builder("i")
                .longOpt("input")
                .desc("训练的语料")
                .argName("Train File")
                .type(File::class.java)
                .hasArgs()
                .required()
                .build())

        options.addOption(Option.builder("o")
                .longOpt("out")
                .desc("输出目录")
                .argName("Save File dir")
                .type(File::class.java)
                .hasArgs()
                .build())

        options.addOption(Option.builder("eva")
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

        options.addOption(Option.builder("n")
                .longOpt("threadNum")
                .desc("线程数")
                .argName("thread num")
                .type(Number::class.java)
                .hasArgs()
                .build())
        return options
    }
}
