package com.mayabot.nlp.starspace

import com.mayabot.nlp.blas.Vector
import com.mayabot.nlp.blas.cosine
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Integer.max
import java.lang.Integer.min

/**
 *
 * @author jimichan
 */
enum class TrainMode {
    /**
     * one Label from RHS is picked as true Label; LHS is the same from input.
     */
    Mode0,

    /**
     * one Label from RHS is picked as true Label; LHS is the bag of the rest RHS labels.
     */
    Mode1,

    /**
     * one Label from RHS is picked as LHS; the bag of the rest RHS labels becomes the true Label.
     */
    Mode2,

    /**
     * one Label from RHS is picked as true Label and another Label from RHS is picked as LHS.
     */
    Mode3,

    /**
     * the first Label from RHS is picked as LHS and the second one picked as true Label.
     */
    Mode4,

    /**
     * continuous bag of words training.
     */
    Mode5
}

enum class FileFormat {
    FastText, LabelDoc
}

enum class LossFunction {
    Hinge, SoftMax
}

enum class Similarity {
    Cosine, Dot
}


/**
 * 训练参数
 */
class Args {

    // The following arguments for the dictionary are optional:


    /**
     * minimal number of Word occurences
     */
    var minCount = 1

    /**
     * minimal number of Label occurences
     */
    var minCountLabel = 1


    var label = "__label__"

    /**
     * max length of Word ngram
     */
    var ngrams = 1

    /**
     * number of buckets
     */
    var bucket = 2000000


    /**
     * takes value in [0, 1, 2, 3, 4, 5], see Training Mode Section. [0]
     */
    var trainMode = TrainMode.Mode0

    /**
     * currently support 'fastText' and 'labelDoc', see File Format Section.
     */
    var fileFormat = FileFormat.FastText

    /**
     * learning rate
     */
    var lr = 0.01


    /**
     * length of embedding vectors
     */
    var dim = 100

    /**
     * number of epochs
     */
    var epoch = 5

    /**
     * max train time (secs)
     */
    var maxTrainTime = 60 * 60 * 24 * 100

    /**
     * number of negatives sampled
     */
    var negSearchLimit = 50

    /**
     * max number of negatives in a batch update
     */
    var maxNegSamples = 10

    /**
     * loss function {hinge, softmax} [hinge]
     */
    var loss = LossFunction.Hinge


    /**
     * margin parameter in hinge loss. It's only effective if hinge loss is used.
     */
    var margin = 0.05

    /**
     * takes value in [cosine, dot]. Whether to use cosine or dot product as similarity function in  hinge loss.
     * It's only effective if hinge loss is used.
     */
    @JvmField
    var similarity = Similarity.Cosine

    /**
     * normalization parameter: we normalize sum of embeddings by deviding Size^p, when p=1, it's equivalent to taking average of embeddings; when p=0, it's equivalent to taking sum of embeddings. [0.5]
     */
    @JvmField
    var p = 0.5


    /**
     * whether to use adagrad in training
     */
    var adagrad = true


    /**
     * whether to use the same embedding matrix for LHS and RHS.
     */
    var shareEmb = true

    /**
     * only used in trainMode 5, the length of the context window for Word level training.
     */
    var ws = 5


    /**
     * dropout probability for LHS features. [0]
     */
    var dropoutLHS = 0.0

    /**
     * dropout probability for RHS features. [0]
     */
    var dropoutRHS = 0.0

    /**
     * initial values of embeddings are randomly generated from normal distribution with mean=0, standard deviation=initRandSd.
     */
    var initRandSd = 0.001

    /**
     * whether to train Word level together with other tasks (for multi-tasking).
     */
    var trainWord = false

    /**
     * if trainWord is true, wordWeight specifies example weight for Word level training examples.
     */
    var wordWeight = 0.5f


    var norm = 1.0


    /**
     * number of threads
     */
    var thread = max(2, min(10, Runtime.getRuntime().availableProcessors() - 1))
        set(value) {
            if (value > Runtime.getRuntime().availableProcessors()) {
                field = Runtime.getRuntime().availableProcessors()
            } else {
                field = value
            }
        }

    var debug = false

    /**
     * whether input file contains weights
     */
    var useWeight = false

    /**
     * whether to run basic text preprocess for input files
     */
    var normalizeText = false


    fun write(out: OutputStream) {
        val data = DataOutputStream(out)
        data.use {
            it.writeInt(dim)
            it.writeInt(epoch)
            it.writeInt(minCount)
            it.writeInt(minCountLabel)
            it.writeInt(maxNegSamples)
            it.writeInt(negSearchLimit)
            it.writeInt(ngrams)
            it.writeInt(bucket)
            it.writeInt(trainMode.ordinal)
            it.writeBoolean(shareEmb)
            it.writeBoolean(useWeight)
            it.writeInt(fileFormat.ordinal)
            it.writeInt(similarity.ordinal)
        }
    }

    fun read(`in`: InputStream) {
        val input = DataInputStream(`in`)
        dim = input.readInt()
        epoch = input.readInt()
        minCount = input.readInt()
        minCountLabel = input.readInt()
        maxNegSamples = input.readInt()
        negSearchLimit = input.readInt()
        ngrams = input.readInt()
        bucket = input.readInt()
        trainMode = TrainMode.values()[input.readInt()]
        shareEmb = input.readBoolean()
        useWeight = input.readBoolean()
        fileFormat = FileFormat.values()[input.readInt()]
        similarity = Similarity.values()[input.readInt()]
    }


    fun norm2Computer(ws: List<XPair>, vec: Vector): Float =
        when (similarity) {
            Similarity.Dot -> Math.pow(ws.size.toDouble(), p).toFloat()
            Similarity.Cosine -> norm2(vec)
        }

    fun similarity(a: Vector, b: Vector): Float =
        when (similarity) {
            Similarity.Dot -> {
                val value = a * b
                checkArgument(!value.isNaN() && !value.isInfinite())
                value
            }
            Similarity.Cosine -> {
                val value = cosine(a, b)
                checkArgument(!value.isNaN() && !value.isInfinite())
                value
            }
        }

    fun norm2(vec: Vector) = Math.max(1.19209e-007f, vec.norm2())

}
