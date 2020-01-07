package com.mayabot.nlp.fasttext.autotune

import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.args.TrainArgs
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class AutotuneStrategy(originalArgs: TrainArgs, seed: Long) {

    private var bestArg: TrainArgs = TrainArgs()
    private var maxDuration = originalArgs.autotuneDuration
    private val rng = Random(seed)
    private var trials = 0
    private var bestMinnIndex = 0
    private var bestDsubExponent = 1
    private var bestNonzeroBucket = 2000000

    var minnChoices = listOf(0, 2, 3)

    init {
        updateBest(originalArgs)
    }

    fun ask(elapsed: Double): TrainArgs {
        val t = min(1.0f, (elapsed / maxDuration).toFloat()).toDouble()
        trials++

        if (trials == 1) {
            return bestArg
        }

        val args = bestArg
        val argsCompute = args.toComputedTrainArgs(ModelName.sup)

        if (!args.isManual("epoch")) {
            args.epoch = updateArgGauss(
                    argsCompute.modelArgs.epoch.toDouble(),
                    1.0,100.0,
                    2.8,2.5,t,false,rng)
                    .toInt()
        }

        if (!args.isManual("lr")) {
            args.lr = updateArgGauss(argsCompute.lr.toDouble(),0.01,5.0,1.9,1.0,t,false,rng)
        }

        if (!args.isManual("dim")) {
            args.dim = updateArgGauss(argsCompute.modelArgs.dim.toDouble(),1.0,1000.0,1.4,0.3,t,false,rng)
                    .toInt()
        }

        TODO()

    }

    private fun  updateArgGauss(value: Double, min: Double, max: Double,
                                            startSigma: Double,
                                            endSigma: Double,
                                            t: Double,
                                            liner: Boolean,
                                            rng: Random): Double {
        var retVal = getArgGauss(value,rng,startSigma,endSigma,t,liner)
        if (retVal > max) {
            retVal = max
        }
        if (retVal < min) {
            retVal = min
        }
        return retVal
    }

    private fun getArgGauss(value: Double,
                            rng: Random,
                            startSigma: Double,
                            endSigma: Double,
                            t: Double,
                            linear: Boolean): Double {

        val stddev = startSigma - ((startSigma - endSigma) / 0.5) * min(0.5, max((t - 0.25), 0.0))

        val normal = { rng.nextGaussian() * stddev }

        val coeff = normal()

        return if (linear) {
            coeff + value
        } else {
            2.0.pow(coeff) * value
        }
    }

    fun getIndex(value: Int, choices: List<Int>): Int {
        TODO()
    }

    fun updateBest(args: TrainArgs) {

    }

}
