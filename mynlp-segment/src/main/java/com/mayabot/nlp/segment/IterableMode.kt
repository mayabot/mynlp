package com.mayabot.nlp.segment

import java.util.*

enum class WordTermIterableMode {
    /**
     * 不输出子词
     */
    TOP,

    /**
     * 输出合并词和子词
     * 北京大学
     *
     * 北京大学 北京 大学
     */
    Overlap,

    /**
     * 只输出子词
     */
    ATOM;

}

class OverlapIterable(val wrap: Iterable<WordTerm>) : Iterable<WordTerm> {

    override fun iterator(): Iterator<WordTerm> {
        return OverlapIterator(wrap.iterator())
    }

}

/**
 * 北京大学     的 学生
 * 北京 大学
 */
class OverlapIterator(val from: Iterator<WordTerm>) : AbstractIterator<WordTerm>() {
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


class AtomIterable(val wrap: Iterable<WordTerm>) : Iterable<WordTerm> {
    override fun iterator(): Iterator<WordTerm> {
        return AtomIterator(wrap.iterator())
    }
}


class AtomIterator(val from: Iterator<WordTerm>) : AbstractIterator<WordTerm>() {
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
                val x = LinkedList(next.subword)
                buffer = x
                setNext(x.poll()!!)
            } else {
                setNext(next)
            }
        } else {
            done()
        }
    }

}


//
//
//class GraphIterable(val wrap: Iterable<WordTerm>) : Iterable<WordTerm> {
//
//    override fun iterator(): Iterator<WordTerm> {
//        return GraphIterator(wrap.iterator())
//    }
//
//
//}
//
//
//class GraphIterator(val from: Iterator<WordTerm>) : AbstractIterator<WordTerm>() {
//    var buffer: LinkedList<WordTerm>? = null
//    override fun computeNext() {
//        val b = buffer
//        if (b != null) {
//            if (b.isEmpty()) {
//                buffer = null
//            } else {
//                setNext(b.poll()!!)
//                return
//            }
//        }
//
//        if (from.hasNext()) {
//            val next = from.next()
//            setNext(next)
//            if (next.hasSubword()) {
//                buffer = LinkedList(next.subword).apply {
//                    //第一个字词的pos是0
//                    //https://lucene.apache.org/core/8_1_0/core/org/apache/lucene/analysis/package-summary.html#package.description
//                    first.posInc = 0
//                }
//            }
//        } else {
//            done()
//        }
//    }
//
//}