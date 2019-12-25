package com.mayabot.nlp.fasttext.args

/**
 *
 */
enum class ModelName constructor(val value: Int) {

    /**
     * CBOW
     */
    cbow(1),

    /**
     * skipgram
     */
    sg(2),

    /**
     * supervised 文本分类模型
     */
    sup(3);


    companion object {

        @Throws(IllegalArgumentException::class)
        fun fromValue(value: Int): ModelName {
            var value = value
            try {
                value -= 1
                return values()[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown ModelName enum second :$value")
            }

        }
    }
}
