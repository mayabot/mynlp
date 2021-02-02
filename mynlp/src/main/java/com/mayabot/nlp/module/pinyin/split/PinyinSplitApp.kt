package com.mayabot.nlp.module.pinyin.split

import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.injector.Singleton
import com.mayabot.nlp.common.utils.CharNormUtils
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.perceptron.PerceptronComputer
import java.io.File

@Singleton
class PinyinSplitService(env: MynlpEnv) {

    val app = PinyinSplitApp.loadDefault(env)

    fun split(text: String) = app.decodeToWordList(text)
}

class PinyinSplitApp(val model: PerceptronModel) {

    private val logic = define.modelComputer(model)

    fun decodeToWordList(sentence: String, convert: Boolean = true): List<String> {
        val result = ArrayList<String>()
        val input = sentence.toCharArray()
        if (convert) {
            CharNormUtils.convert(input)
        }

        val output = logic.decodeModel(input)

        var p = 0
        for (i in 0 until output.size) {
            val f = output[i]
            if (f == "S" || f == "E") {
                result += sentence.substring(p, i + 1)
                p = i + 1
            }
        }
        if (p < sentence.length) {
            result += sentence.substring(p, sentence.length)
        }

        return result
    }

    companion object {

        const val modelPrefix = "pinyin-split-model"

        val define = PinyinSplitDefinition()

        fun load(file: File): PinyinSplitApp {
            return PinyinSplitApp(PerceptronModel.load(file))
        }

        fun loadDefault(env: MynlpEnv): PinyinSplitApp {
            return PinyinSplitApp(PerceptronModel.loadFromNlpResource(modelPrefix, env))
        }
    }
}