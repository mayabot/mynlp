package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.utils.IntArrayList
import com.mayabot.nlp.fasttext.utils.logger
import com.mayabot.nlp.fasttext.utils.loggerln

class Meter(
        val metrics: Metrics = Metrics(),
        var nexamples: Long = 0,
        var labelMetrics: HashMap<Int, Metrics> = HashMap()
) {
    fun HashMap<Int, Metrics>.find(key: Int): Metrics {
        return getOrPut(key) { Metrics() }
    }

    fun log(labels: IntArrayList, predictions: List<ScoreIdPair>) {
        nexamples++
        metrics.gold += labels.size()
        metrics.predicted += predictions.size

        for (prediction in predictions) {
            labelMetrics.find(prediction.id).predicted++

            val score = kotlin.math.exp(prediction.score).toDouble()
            var gold = 0.0
            if (labels.contains(prediction.id)) {
                labelMetrics.find(prediction.id).predictedGold++
                metrics.predictedGold++
                gold = 1.0
            }
            labelMetrics.find(prediction.id).scoreVsTrue.add(score to gold)
        }

        labels.forEach { label ->
            labelMetrics.find(label).gold++
        }
    }

    fun precision(i: Int): Double {
        return labelMetrics.find(i).precision()
    }

    fun recall(i: Int): Double {
        return labelMetrics.find(i).recall()
    }

    fun f1Score(i: Int): Double {
        return labelMetrics.find(i).f1Score()
    }

    fun precision(): Double {
        return metrics.precision()
    }

    fun recall(): Double {
        return metrics.recall()
    }

    fun f1Score(): Double {
        var precision = this.precision()
        val recall = this.recall()
        if (precision + recall != 0.0) {
            return 2 * precision * recall / (precision + recall)
        }
        return Double.NaN
    }

    fun writeGeneralMetrics(k: Int): String {
        val sb = StringBuilder()
        sb.append("N\t$nexamples\n")
        sb.append("P@$k\t${String.format("%.3f", metrics.precision())}\n")
        sb.append("R@$k\t${String.format("%.3f", metrics.recall())}\n")
        return sb.toString()
    }

    /**
     * 打印结果，[preLabel]打印每个label的详细
     */
    fun print(dict: Dictionary, k: Int, perLabel: Boolean = false) {
        if (perLabel) {
            fun writeMetric(name: String, value: Double) {
                val sb = "$name : ${if (value.isFinite()) "%.6f".format(value) else "--------"} "
                print(sb)
            }
            for (labelId in 0 until dict.nlabels) {
                writeMetric("F1-Score", this.f1Score(labelId))
                writeMetric("Precision", this.precision(labelId))
                writeMetric("Recall", this.recall(labelId))
                println(" ${dict.getLabel(labelId)}")
            }
        }
        println(writeGeneralMetrics(k))
    }


    class Metrics(var gold: Long = 0,
                  var predicted: Long = 0,
                  var predictedGold: Long = 0) {

        var scoreVsTrue: MutableList<Pair<Double, Double>> = mutableListOf()

        fun precision(): Double {
            if (predicted == 0L) {
                return Double.NaN
            }
            return predictedGold / predicted.toDouble()
        }

        fun recall(): Double {
            if (gold == 0L) {
                return Double.NaN
            }
            return predictedGold / gold.toDouble()
        }

        fun f1Score(): Double {
            if (predicted + gold == 0L) {
                return Double.NaN
            }
            return 2 * predictedGold / (predicted + gold).toDouble()
        }

    }
}
