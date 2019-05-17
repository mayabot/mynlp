/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.segment.lexer.crf

import java.util.regex.Pattern

/**
 * @author jimichan
 */
class FeatureTemplateGroup(templates: List<String>) {

    val size: Int

    val list = ArrayList<FeatureTemplate>()

    init {
        for (template in templates) {
            list.add(FeatureTemplate(template))
        }
        this.size = templates.size
    }

    companion object {
        val BOS = arrayOf("_B-1", "_B-2", "_B-3", "_B-4", "_B-5", "_B-6", "_B-7", "_B-8")
        val EOS = arrayOf("_B+1", "_B+2", "_B+3", "_B+4", "_B+5", "_B+6", "_B+7", "_B+8")
    }

}


// U6:%x[1,0]/%x[2,0]
/**
 * @author jimichan
 */
class FeatureTemplate(template: String) {

    val list = ArrayList<FeatureTemplateElement>(10)

    private val pattern = Pattern.compile("%x\\[(-?\\d*),(\\d*)]")

    init {
        val matcher = pattern.matcher(template)
        var start = 0
        while (matcher.find()) {
            val offset = matcher.start()
            val end = matcher.end()
            if (offset > start) {
                list.add(FeatureTemplateElement(template.substring(start, offset)))
            }

            list.add(FeatureTemplateElement(matcher.group(1).toInt(), matcher.group(2).toInt()))

            start = end
        }

        val first = list.removeAt(0)

        list.add(first)
    }

    override fun toString(): String {
        return list.joinToString(separator = "")
    }
}

enum class FeatureTemplateElementType {
    String, Offset
}

class FeatureTemplateElement {

    constructor(value: String) {
        if (value.startsWith("U") && value.endsWith(":")) {
            this.value = ":" + value.substring(0, value.length - 1)
        } else {
            this.value = value
        }

    }

    constructor(offset: Int, col: Int) {
        this.offset = offset
        this.col = col
        this.type = FeatureTemplateElementType.Offset
    }

    var value: String = ""
    var offset: Int = 0
    var col: Int = 0
    var type = FeatureTemplateElementType.String

    override fun toString(): String {
        if (type == FeatureTemplateElementType.String) {
            return value
        } else {
            return "%x[$offset,$col]"
        }
    }
}
