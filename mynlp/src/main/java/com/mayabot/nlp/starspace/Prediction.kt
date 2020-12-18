package com.mayabot.nlp.starspace

import com.mayabot.nlp.blas.Vector


data class Prediction(var score: Float, var second: Int)

class StarSpacePrediction(private val model: StarSpace, basedoc: String?) {

    var baseDocVectors: MutableList<Vector> = ArrayList()

    var baseDocs: MutableList<List<XPair>> = ArrayList()

    init {
        val (x, y) = model.loadBaseDocs(basedoc)
        baseDocs = y
        baseDocVectors = x
    }

    fun predictOne(doc: String): List<Prediction> {
        return predictOne(model.dict.parseDoc(doc), 5)
    }

    fun predictOne(doc: String, k: Int): List<Prediction> {
        return predictOne(model.dict.parseDoc(doc), k)
    }

    fun predictOne(input: List<XPair>, k: Int): List<Prediction> {

        val lhsM = model.projectLHS(input)

        val topMax = TopMaxK(k)

        for (i in baseDocVectors.indices) {
            val score = model.args.similarity(lhsM, baseDocVectors[i])
            topMax.push(i, score)
        }

        return topMax.resort().map { Prediction(it.second, it.first) }
    }

}
