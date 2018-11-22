import com.mayabot.nlp.perceptron.solution.pos.POSPerceptronTrainer
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File

fun main(args: Array<String>) {
    val model = POSPerceptronTrainer().train(File("data/pku.txt"), 1, 8)
//
    model.save(File("data/pos-big.bin"))
//    val model = POSPerceptron.loadModel(File("data/pos-all-dat.bin"))
//    val words = "陈汝烨 余额宝 的 规模 增长 一直 呈现 不断 加速 , 的 状态".split(" ")

    val words2 = CharNormUtils.convert("人民 收入 和 生活 水平 进一步 提高").split(" ")
    val result = model.decode(words2)
    println(words2.zip(result))
}
