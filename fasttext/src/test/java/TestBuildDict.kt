import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.args.TrainArgs
import com.mayabot.nlp.fasttext.dictionary.buildFromFile
import com.mayabot.nlp.fasttext.train.whitespaceSplitter
import java.io.File

//fun main() {
//    val source = FileTrainExampleSource(
//            whitespaceSplitter, File("fasttext/src/test/resources/agnews/ag.train")
//    )
//
//    val args = TrainArgs()
//
//    val dict = buildFromFile(args.toComputedTrainArgs(ModelName.cbow),source,200000)
//}
