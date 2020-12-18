package com.mayabot.nlp.starspace

import com.mayabot.nlp.starspace.TrainMode.Mode0
import com.mayabot.nlp.starspace.TrainMode.Mode5
import java.util.regex.Pattern

/**
 * 解析训练文件和test文件。每一行都是一个example。
 *
 */
interface DataParser {
//    fun parse(line: String): ParseResult?{
//        return this.parse(line,java.util.regex.Pattern.compile("[\t ]"))
//    }

    fun parse(line: String, sep: Pattern): ParseResult?
}

class ParseResult {
    @JvmField
    var weight = 1.0f

    @JvmField
    var lhsTokens: List<XPair> = listOf()

    @JvmField
    var rhsTokens: List<XPair> = listOf()

    @JvmField
    var rhsFeatures: List<List<XPair>> = listOf()

}

/**
 * This is the basic class of data parsing.
 * It provides essential functions as follows:
 * - parse(input, output):
 * takes input as a line of string (or a vector of string tokens)
 * and return output result which is one example contains l.h.s. features
 * and r.h.s. features.
 *
 * - parseForDict(input, tokens):
 * takes input as a line of string, output tokens to be added for building
 * the dictionary.
 *
 *
 * - check(example):
 * checks whether the example is a valid example.
 *
 *
 * - addNgrams(input, output):
 * add ngrams from input as output.
 *
 *
 * One can write different parsers for data with different format.
 */
open class FastTextDataParser(
    val dictionary: Dictionary,
    args: Args
) : DataParser {

    protected val useWeight = args.useWeight
    protected val normalizeText = args.normalizeText
    protected val trainMode = args.trainMode


//    fun resetDict(dictionary: Dictionary) {
//        this.dictionary = dictionary
//    }

    @JvmOverloads
    override fun parse(line: String, sep: Pattern): ParseResult? {
        val tokens = line.split(sep).asSequence().map { it.trim() }.filterNot { it.isNullOrEmpty() }.toList()
        val lhsTokens = ArrayList<XPair>()
        val rhsTokens = ArrayList<XPair>()

        val rslts = ParseResult().apply {
            this.rhsTokens = rhsTokens
            this.lhsTokens = lhsTokens
        }

        for (token in tokens) {
            if (token.startsWith("__weight__")) {
                val pos = token.indexOf(":")
                if (pos != -1) {
                    rslts.weight = token.substring(pos + 1).toFloat()
                }
                continue
            }

            var t = token
            var weight = 1.0f

            if (useWeight) {
                val pos = token.indexOf(":")
                if (pos != -1) {
                    t = token.substring(0, pos)
                    weight = token.substring(pos + 1).toFloat()
                }
            }

            if (normalizeText) {
                t = NormalizeText.normalize(t)
            }

            val wid = dictionary.getId(t)
            if (wid < 0) {
                continue
            }

            val type = dictionary.getType(wid)
            if (type === EntryType.Word) {
                lhsTokens.add(XPair(wid, weight))
            }

            if (type === EntryType.Label) {
                rhsTokens.add(XPair(wid, weight))
            }
        }//for

        dictionary.addNgrams(tokens, lhsTokens)

        return if (check(rslts)) {
            rslts
        } else null
    }

    fun check(example: ParseResult): Boolean {
        if (trainMode == Mode0) {
            //require lhs and rhs
            return !example.rhsTokens.isEmpty() && !example.lhsTokens.isEmpty()
        }
        return if (trainMode == Mode5) {
            // only requires lhs.
            !example.lhsTokens.isEmpty()
        } else {
            // lhs is not required, but rhs should contain at least 2 example
            example.rhsTokens.size > 1
        }
    }
}


/**
 * This is the parser class for the case where we have features
 * to represent labels. It overrides a few key functions such as
 * parse(input, output) and check(example) in the basic Parser class.
 */
class LayerDataParser(dict: Dictionary, args: Args) :
    FastTextDataParser(dict, args), DataParser {

    override fun parse(line: String, sep: Pattern): ParseResult? {

        val rslt = ParseResult()

        val parts = line.splitToSequence('\t').map { it.trim() }.filterNot { it.isNullOrEmpty() }.toList()

        var startIdx = 0

        if (trainMode == Mode0) {
            // the first part is input features
            rslt.lhsTokens = string2Pair(parts[startIdx])
            startIdx += 1
        }

        val rhsFeature = ArrayList<List<XPair>>(parts.size)

        rslt.rhsFeatures = rhsFeature

        for (i in startIdx until parts.size) {
            val feats = string2Pair(parts[i])
            if (feats.isNotEmpty()) {
                rhsFeature.add(feats)
            }
        }

        val isValid = if (trainMode == Mode0) {
            rslt.lhsTokens.isNotEmpty() && rslt.rhsFeatures.isNotEmpty()
        } else {
            // need to have at least two examples
            rslt.rhsFeatures.size > 1
        }

        return if (isValid) {
            rslt
        } else {
            null
        }
    }

    private fun string2Pair(s: String): List<XPair> {


        // split each part into tokens
        val tokens = s.split(' ')

        val feats = ArrayList<XPair>(tokens.size)

        var startIdx = 0
        var exWeight = 1.0f

        if (tokens[0].indexOf("__weight__") >= 0) {
            val pos = tokens[0].indexOf(':')

            if (pos > 0) {
                exWeight = tokens[0].substring(pos + 1).toFloat()
            }
            startIdx = 1
        }

        for (i in startIdx until tokens.size) {
            var t = tokens[i]
            if (t.isNullOrBlank()) {
                continue
            }
            var weight = 1.0f
            val token = tokens[0]
            if (useWeight) {
                val pos = token.indexOf(':')
                if (pos > 0) {
                    t = token.substring(0, pos)
                    weight = token.substring(pos + 1).toFloat()
                }
            }

            if (normalizeText) {
                t = NormalizeText.normalize(t)
            }

            val wid = dictionary.getId(t)
            if (wid != -1) {
                feats.add(XPair(wid, weight * exWeight))
            }
        }

        dictionary.addNgrams(tokens, feats)

        return feats
    }

}