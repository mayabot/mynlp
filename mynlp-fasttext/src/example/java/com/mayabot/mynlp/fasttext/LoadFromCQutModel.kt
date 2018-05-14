package com.mayabot.mynlp.fasttext

fun main(args: Array<String>) {
    val train = FastText.loadFasttextBinModel("data/fasttext/model.ftz")
    //val text = "court hears interstate wine sales case ( ap ) , ap - the supreme court considered tuesday whether state alcoholic beverage regulations put in place 70 years ago , after prohibition was lifted , should remain the law of the land in the internet age ."

    //val predict = train.predict(text.split(" "), 4)
    //println(predict)

    AgnewsTest.predict(train)

    train.saveModel("data/fasttext/qmodel")

    val train2 = FastText.loadModel("data/fasttext/qmodel")

    AgnewsTest.predict(train2)
}
