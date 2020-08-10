///*
// * Copyright 2018 mayabot.com authors. All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.mayabot.nlp.common
//
//import com.google.common.collect.ImmutableMap
//import com.google.common.primitives.Ints
//import java.io.DataInput
//import java.io.DataOutput
//import java.io.IOException
//import java.util.function.Function
//
///**
// * 对标签-频次的封装
// * 参考HanLP https://github.com/hankcs/HanLP 中的实现
// * 做了一些改动，labelMap使用ImmutableMap。序列化采用FastJson对信息打包
// *
// * @author hankcs
// * @author jimichan
// */
//class EnumFreqPair<E : Enum<*>>(
//    var map: Map<E, Int> = mapOf()
//) {
//
//    /**
//     * 创建只有一个标签的条目
//     *
//     * @param label
//     * @param frequency
//     */
//    constructor(label: E, frequency: Int) : this( mapOf(label to frequency))
//
//    constructor(label: E, frequency: Int, label2: E, freq: Int) : this(mapOf(label to frequency, label2 to freq))
//
//    constructor(label: E, frequency: Long) : this(mapOf(label to frequency.toInt()))
//
//    constructor(vararg labels: E) : this() {
//
//        val map = HashMap<E,Int>()
//
//        for (label in labels) {
//            map[label] = 1
//        }
//
//        this.map = map.toMap()
//    }
//
//    /**
//     * 优化
//     * <pre>
//     * null -> "NULL"
//     * a:1  -> a:1
//     * {a:1,b:2}  -> a:1,b:2
//    </pre> *
//     *
//     * @param out
//     */
//    fun writeItem(out: DataOutput) {
//        try {
//
//            val size: Int = map.size
//
//            when (size) {
//                0 -> out.writeUTF("NULL")
//                1 -> {
//                    val next = map.entries.first()
//                    val name = next.key.name
//                    val f = next.value
//                    out.writeUTF("$name,$f")
//                }
//                else -> {
//                    val sb = StringBuilder()
//                    var count = 0
//                    for (e in map.entries) {
//                        count++
//                        sb.append(e.key.name).append(",").append(e.value)
//                        if (count != size) {
//                            sb.append(",")
//                        }
//                    }
//                    out.writeUTF(sb.toString())
//                }
//            }
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        }
//    }
//
//    fun readItem(`in`: DataInput, function: Function<String, E>) {
//        try {
//            val json = `in`.readUTF()
//
//            val kmap = if ("NULL" == json) {
//                emptyMap()
//            } else {
//
//                val map = HashMap<E,Int>()
//
//                val split: Array<String> = json.split(",").toTypedArray()
//                var i = 0
//
//                while (i < split.size) {
//                    map[function.apply(split[i])] = split[i + 1].toInt()
//                    i += 2
//                }
//                map.toMap()
//            }
//
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
//    }
//
//    /**
//     * 只有一个值的，key
//     *
//     * @return E
//     */
//    fun oneKey(): E? {
//        return map.keys.firstOrNull()
//    }
//
//    fun keySet(): Set<E> {
//        return map.keys
//    }
//
//    fun size(): Int {
//        return map.size
//    }
//
//    fun containsLabel(label: E): Boolean {
//        return map.containsKey(label)
//    }
//
//    fun getFrequency(label: E): Int {
//        return map[label]?:0
//    }
//
//    override fun toString(): String {
//        return map.toString()
//    }
//
//    companion object {
//        /**
//         * 创建一个条目，其标签频次都是1，各标签由参数指定
//         *
//         * @param x
//         */
//        @SafeVarargs
//        fun <E : Enum<E>> create(vararg x: E): EnumFreqPair<E> {
//            return EnumFreqPair(*x)
//        }
//
//        fun <E : Enum<*>> create(params: MutableList<String>, f: Function<String, E>): EnumFreqPair<E> {
//
//            val builder = HashMap<E,Int>()
//
//            val ite = params.iterator()
//
//            while (ite.hasNext()) {
//                val key = ite.next()
//                val value = ite.next()
//                builder.put(f.apply(key), value.toInt())
//            }
//
//            return EnumFreqPair(builder.toMap())
//        }
//    }
//}