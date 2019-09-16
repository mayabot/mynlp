package com.mayabot.nlp.pinyin.split

import com.google.inject.Singleton
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.perceptron.Perceptron
import com.mayabot.nlp.perceptron.PerceptronModel
import com.mayabot.nlp.utils.CharNormUtils
import java.io.File

object PinyinSplits{

    val service by lazy { Mynlps.instanceOf<PinyinSplitService>() }

    @JvmStatic
    fun split(text: String) = service.split(text)
}

@Singleton
class PinyinSplitService{

    val app = PinyinSplitApp.loadDefault()

    fun split(text:String) = app.decodeToWordList(text)
}

class PinyinSplitApp(val model: Perceptron) {

    private val logic = PinyinSplitDefinition()

    fun decodeToWordList(sentence: String, convert: Boolean = true): List<String> {
        val result = ArrayList<String>()
        val input = sentence.toCharArray()
        if (convert) {
            CharNormUtils.convert(input)
        }

        val output = logic.decodeModel(model, input)

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

        fun load(file: File):PinyinSplitApp {
            return PinyinSplitApp(PerceptronModel.load(file))
        }

        fun loadDefault():PinyinSplitApp{
            return PinyinSplitApp(PerceptronModel.loadFromNlpResouce(modelPrefix))
        }
    }
}