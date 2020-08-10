package com.mayabot.nlp.segment.plugins.correction

/**
 * 第几套/房
 */
class CorrectionWord(
        val raw: String,
        @JvmField
        val path: String,
        val words: IntArray
) {


    override fun toString(): String {
        return "CorrectionWord{" + "path='" + path + '\'' +
                ", raw='" + raw + '\'' +
                ", words=" + words +
                '}'
    }

    companion object {
//        var splitter = Splitter.on("/").trimResults().omitEmptyStrings()

        /**
         * 第几套/房
         *
         * @param line
         * @return CorrectionWord
         */
        @kotlin.jvm.JvmStatic
        fun parse(line: String): CorrectionWord {

            val raw = line.trim()
            val list = raw.split("/").map { it.trim() }.filter { it.isNotEmpty() }
            val path = list.joinToString("")
            val words = list.map { it.length }.toIntArray()
            return CorrectionWord(raw, path, words)
        }
    }
}
