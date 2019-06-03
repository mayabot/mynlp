package com.mayabot.nlp.segment

import java.util.*

enum class IterableMode {
    DEFAULT,
    GRAPH,
    FLATTEN
}

class GraphIterable(val wrap: Iterable<WordTerm>) : Iterable<WordTerm> {

    override fun iterator(): Iterator<WordTerm> {
        return GraphIterator(wrap.iterator())
    }


}


class GraphIterator(val from: Iterator<WordTerm>) : AbstractIterator<WordTerm>() {
    var buffer: LinkedList<WordTerm>? = null
    override fun computeNext() {
        val b = buffer
        if (b != null) {
            if (b.isEmpty()) {
                buffer = null
            } else {
                setNext(b.poll()!!)
                return
            }
        }

        if (from.hasNext()) {
            val next = from.next()
            setNext(next)
            if (next.hasSubword()) {
                buffer = LinkedList(next.subword).apply {
                    //第一个字词的pos是0
                    //https://lucene.apache.org/core/8_1_0/core/org/apache/lucene/analysis/package-summary.html#package.description
                    first.posInc = 0
                }
            }
        } else {
            done()
        }
    }

}


class FlattenIterable(val wrap: Iterable<WordTerm>) : Iterable<WordTerm> {

    override fun iterator(): Iterator<WordTerm> {
        return FlattenIterator(wrap.iterator())
    }
}


class FlattenIterator(val from: Iterator<WordTerm>) : AbstractIterator<WordTerm>() {
    var buffer: LinkedList<WordTerm>? = null
    override fun computeNext() {
        val b = buffer
        if (b != null) {
            if (b.isEmpty()) {
                buffer = null
            } else {
                setNext(b.poll()!!)
                return
            }
        }

        if (from.hasNext()) {
            val next = from.next()
            if (next.hasSubword()) {
                val b = LinkedList(next.subword)
                buffer = b
                setNext(b.poll()!!)
            } else {
                setNext(next)
            }
        } else {
            done()
        }
    }

}