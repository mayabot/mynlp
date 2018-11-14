package com.mayabot.nlp.perceptron

import org.trie4j.MapTrie
import org.trie4j.doublearray.MapDoubleArray
import org.trie4j.patricia.MapPatriciaTrie
import java.io.*


//private fun addTransitionFeatures() {
//    for (i in tagSet.indices) {
//        idOf("BL=" + labelSet.get(i))
//    }
//    idOf("BL=_BL_")
//}

class FeatureSet(
        private val features: MapTrie<Int>
) {
    private val size = features.size()

    fun featureId(feature: String): Int {

        return features.get(feature) ?: -1
    }

    fun size() = size

    companion object {

        fun read(input: DataInputStream): FeatureSet {
            val data = ObjectInputStream(input)
            val x = MapDoubleArray<Int>()
            x.readExternal(data)
            return FeatureSet(x)
        }
    }

    fun save(out: DataOutputStream) {
        val dataOut = ObjectOutputStream(out)
        when (features) {
            is Externalizable -> {
                features.writeExternal(dataOut)
            }
            else -> {
                throw RuntimeException()
            }
        }
//        features.writeExternal(dataOut)
        dataOut.flush()
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
        //val trie = MapDoubleArray(map)
        val trie = MapDoubleArray<Int>(map)
        println("Tree Size ${trie.size()}")
        return FeatureSet(trie)
    }
}