package com.mayabot.nlp.perceptron

import org.trie4j.louds.MapTailLOUDSTrie
import org.trie4j.patricia.MapPatriciaTrie
import java.io.*


//private fun addTransitionFeatures() {
//    for (i in tagSet.indices) {
//        idOf("BL=" + labelSet.get(i))
//    }
//    idOf("BL=_BL_")
//}

class FeatureSet(
        private val features: MapTailLOUDSTrie<Int>
) {
    private val size = features.size()

    fun featureId(feature: String): Int {

        return features.get(feature) ?: -1
    }

    fun size() = size

    companion object {

        fun load(file: File): FeatureSet {
            return file.inputStream().buffered().use {
                val inData = ObjectInputStream(it)
                val x = MapTailLOUDSTrie<Int>()
                x.readExternal(inData)
                FeatureSet(x)
            }
        }

        fun read(input: DataInputStream): FeatureSet {
            val data = ObjectInputStream(input)
            val x = MapTailLOUDSTrie<Int>()
            x.readExternal(data)
            return FeatureSet(x)
        }
    }

    fun save(out: DataOutputStream) {
        val dataOut = ObjectOutputStream(out)
        features.writeExternal(dataOut)
        dataOut.flush()
    }

    fun save(file: File) {
        file.outputStream().buffered().use {
            val dataOut = ObjectOutputStream(it)
            features.writeExternal(dataOut)
        }
    }
}


class FeatureSetBuilder {

    val map = MapPatriciaTrie<Int>()

    var id = 0
    fun put(feature: String) {
        if (!map.contains(feature)) {
            map.insert(feature, id++)
        }
    }

    fun build(): FeatureSet {
        val trie = MapTailLOUDSTrie(map)
        println("Tree Size ${trie.size()}")
        return FeatureSet(trie)
    }
}