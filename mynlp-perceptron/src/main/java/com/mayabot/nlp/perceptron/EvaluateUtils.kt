package com.mayabot.nlp.perceptron


/**
 * 简单评估。只考虑标签相等性
 */
fun simpleEvaluate(model: Perceptron, samples: List<TrainSample>) : EvaluateResult{

    var total = 0
    var right = 0

    samples.forEach { sample->
        total += sample.label.size
        val gold = sample.label
        val pred = model.decode(sample.featureSequence)
        for (i in 0 until sample.label.size) {
            if(gold[i] == pred[i]){
                right++
            }
        }
    }

    return EvaluateResult(total,total,right)
}

/**
 * 计算分词正确的词数
 */
fun wordCorrect(gold: List<String>, pred: List<String>): Int {
    var goldIndex = 0
    var predIndex = 0
    var goldLen = 0
    var predLen = 0

    var correct = 0
    while (goldIndex < gold.size && predIndex < pred.size) {
        if (goldLen == predLen) {
            if (gold[goldIndex] == pred[predIndex]) {
                correct++
                goldLen += gold[goldIndex].length
                predLen += gold[goldIndex].length
                goldIndex++
                predIndex++
            } else {
                goldLen += gold[goldIndex].length
                predLen += pred[predIndex].length
                goldIndex++
                predIndex++
            }
        } else if (goldLen < predLen) {
            goldLen += gold[goldIndex].length
            goldIndex++
        } else {
            predLen += pred[predIndex].length
            predIndex++
        }
    }
    return correct
}