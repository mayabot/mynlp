import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.dictionary.MAX_VOCAB_SIZE
import com.mayabot.nlp.fasttext.dictionary.buildFromFile
import com.mayabot.nlp.fasttext.train.TrainSampleList
import com.mayabot.nlp.fasttext.train.whitespaceSplitter
import java.io.File

fun main() {
    val sources = listOf(TrainSampleList(File("fasttext/data/agnews/ag.train")))


    val args = TrainArgs().toComputedTrainArgs(ModelName.cbow)
//
    val dict = buildFromFile(args,sources, MAX_VOCAB_SIZE)

    println(dict.getLabel(0))
    println(dict.getLabel(1))
    println(dict.getLabel(2))
    println(dict.getLabel(3))
}
