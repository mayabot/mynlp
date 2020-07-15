package com.mayabot.nlp.segment.plugins.atom

import com.mayabot.nlp.utils.CartesianList
import java.util.*
import kotlin.collections.ArrayList

fun TreeMap<String, TemplateType>.addTemplate(template: String, type: TemplateType) {
    parseTemplate(template).forEach { this[it] = type }
}

enum class TemplateType {
    TIME,//0
    NUMBER,//1
    MQ,//2
    WORD,//3
    CONNECT//4
}

/**
 * {Z[1,2]}
 * {Z[1-2]}
 * {(日|月)}
 */
fun parseTemplate(template: String): List<String> {
    val pattern = Regex("(\\{(.+?)\\})|(.+?)")

    val list = ArrayList<List<String>>()

    pattern.findAll(template).forEach { mr ->
        var part = mr.value
        if (part.startsWith("{") && part.endsWith("}")) {
            part = part.substring(1, part.length - 1)
        }

        if (part.startsWith("(") && part.endsWith(")")) {
            list.add(part.substring(1, part.length - 1).split("|").toList())
        } else if (part.contains("[") || part.contains("|")) {
            val st = ArrayList<String>()
            val e = part.substring(0, part.indexOf("[")).split("|")

            val range = part.substring(part.indexOf("[") + 1, part.lastIndexOf("]"))
            if (range.contains(",")) {
                range.split(",").map { it.toInt() }.forEach { n ->
                    e.forEach { st.add(it.repeat(n)) }
                }
            } else if (range.contains("-")) {
                val start = range.split("-")[0].toInt()
                val end = range.split("-")[1].toInt()
                for (n in start..end) {
                    e.forEach { st.add(it.repeat(n)) }
                }
            } else {
                e.forEach { st.add(it.repeat(range.toInt())) }
            }
            list.add(st)
        } else {
            list.add(listOf(part))
        }
    }


    val clist = CartesianList.create(list)

    val result = clist.map {
        it.joinToString(separator = "")
    }
    return result
}