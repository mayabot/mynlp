package com.mayabot.nlp.perceptron

import org.trie4j.MapNode
import org.trie4j.Node

fun main(args: Array<String>) {
//    val lines = File("data/pcws/features.txt").readLines()
//
//    val list = lines.map { it.split("\t").first() }.toList()
//    val treeset = TreeSet(list)
//
//    val list2 = treeset.toList()
//
//    var maker = DATDoubleArrayMaker(list2)
//
//    println("-----------")
//    val t1 = System.currentTimeMillis()
//    maker.build()
//    val t2 = System.currentTimeMillis()
//
//    println("use time "+(t2-t1))
//
//    println(maker.base.size)
    println(Float.MAX_VALUE)


}

fun accessNode(node: Node) {
    if (node.isTerminate) {
        when (node) {
            is MapNode<*> -> {
                println(String(node.letters) + " ---- " + node.value)
            }
        }
        node.children.forEach { x ->
            accessNode(x)
        }
    } else {
        node.children.forEach { x ->
            accessNode(x)
        }
    }
}
