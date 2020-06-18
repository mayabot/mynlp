/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.mayabot.nlp.utils

import java.util.*

/**
 * 计算笛卡尔
 * @author Louis Wasserman
 */
class CartesianList<E>(axes: List<List<E>>) : AbstractList<List<E>>(), RandomAccess {

    @Transient
    private val axes: List<List<E>> = axes

    @Transient
    private val axesSizeProduct: IntArray


    init {
        val axesSizeProduct = IntArray(axes.size + 1)
        axesSizeProduct[axes.size] = 1
        try {
            for (i in axes.size - 1 downTo 0) {
                axesSizeProduct[i] = axesSizeProduct[i + 1] * axes.get(i).size
            }
        } catch (e: ArithmeticException) {
            throw IllegalArgumentException(
                    "Cartesian product too large; must have size at most Integer.MAX_VALUE")
        }
        this.axesSizeProduct = axesSizeProduct
    }


    private fun getAxisIndexForProductIndex(index: Int, axis: Int): Int {
        return index / axesSizeProduct[axis + 1] % axes.get(axis).size
    }


    override fun get(index: Int): List<E> {

        check(index in 0 until size)

        return object : AbstractList<E>() {

            override val size: Int
                get() = axes.size

            override fun get(index: Int): E? {
                check(index in 0 until size)
                val axisIndex = getAxisIndexForProductIndex(index, index)
                return axes[index][axisIndex]
            }

            val isPartialView: Boolean
                get() = true
        }
    }

    override val size: Int
        get() = axesSizeProduct[0]

    companion object {
        fun <E> create(lists: List<List<E>>): List<List<E>> {

            val axesBuilder = ArrayList<List<E>>()
            for (list in lists) {
                val copy: List<E> = list.toList()
                if (copy.isEmpty()) {
                    return emptyList()
                }
                axesBuilder.add(copy)
            }
            return CartesianList(axesBuilder.toList())
        }
    }

}

fun main() {
    val list = CartesianList.create(listOf(
            listOf("a", "b"),
            listOf("2", "3", "1"),
            listOf("J", "Y", "B")
    ))
    list.forEach {
        println(it)
    }
}