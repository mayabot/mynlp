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
package com.mayabot.nlp.segment

import com.google.inject.Binder
import com.google.inject.Module
import com.mayabot.nlp.MynlpEnv
import java.io.File
import java.util.regex.Pattern

class SegmentModule(private val env: MynlpEnv) : Module {

    override fun configure(binder: Binder?) {
        env.registeResourceMissing("segment") { rsName, env -> this.processMiss(rsName, env) }
        update()
    }

    /**
     * 启动的时候。如果data目录夹下面存在不同版本的资源文件。当前配置文件里面的版本又是新的。
     * 那么启动时自动下载最新的文件。
     */
    private fun update() {
        val dataDir = env.dataDir
        val pattern = Pattern.compile("^(.*?)-(\\d+[\\.\\d]+)\\.jar$")
        if (dataDir.exists() && dataDir.canRead()) {

            //已经存在的数据包的去除版本的名字
            val nameSet = dataDir.listFiles().filter { pattern.matcher(it.name).matches() }
                    .map {
                        val matcher = pattern.matcher(it.name)
                        matcher.find()
                        "${matcher.group(1)}.jar"
                    }.toSet()

            metaList.forEach { meta ->
                if (meta.fileName in nameSet) {
                    if (!File(env.dataDir, meta.fileNameWithVersion()).exists()) {
                        env.download(meta.fileNameWithVersion())
                    }
                }
            }
        }
    }

    /**
     * 对应的资源没有找到，自动下载
     */
    private fun processMiss(rsName: String, env: MynlpEnv): Boolean {

        synchronized(lock) {
            val meta = mapIndex[rsName]

            if (meta != null) {
                val file = env.download(meta.fileNameWithVersion())

                if (file != null && file.exists()) {
                    return true
                }
            }
        }

        return false
    }

    private val metaList: List<ResourceMeta> = listOf(
            ResourceMeta(
                    name = "CoreDict",
                    version = "1.0.0",
                    fileName = "mynlp-resource-coredict.jar",
                    content = listOf("core-dict/CoreDict.txt", "core-dict/CoreDict.bigram.txt")
            ),
            ResourceMeta(
                    name = "POS",
                    version = "1.0.0",
                    fileName = "mynlp-resource-pos.jar",
                    content = listOf("pos-model/feature.txt", "pos-model/label.txt", "pos-model/parameter.bin")
            ),
            ResourceMeta(
                    name = "NER",
                    version = "1.0.0",
                    fileName = "mynlp-resource-ner.jar",
                    content = listOf(
                            "ner-model/feature.txt", "ner-model/label.txt", "ner-model/parameter.bin",
                            "person-name-model/feature.txt",
                            "person-name-model/parameter.bin"
                    )
            ),
            ResourceMeta(
                    name = "CUSTOM",
                    version = "1.0.0",
                    fileName = "mynlp-resource-custom.jar",
                    content = listOf(
                            "custom-dict/CustomDictionary.txt",
                            "custom-dict/上海地名.txt",
                            "custom-dict/人名词典.txt",
                            "custom-dict/全国地名大全.txt",
                            "custom-dict/机构名词典.txt",
                            "custom-dict/机构名词典.txt",
                            "custom-dict/现代汉语补充词库.txt"
                    )
            ),
            ResourceMeta(
                    name = "STOPWORD",
                    version = "1.0.0",
                    fileName = "mynlp-resource-stopword.jar",
                    content = listOf(
                            "stopword-dict/stopwords.txt"
                    )
            ),
            ResourceMeta(
                    name = "CWS",
                    version = "1.0.0",
                    fileName = "mynlp-resource-cws.jar",
                    content = listOf(
                            "cws-model/feature.dat",
                            "cws-model/feature.txt",
                            "cws-model/parameter.bin"
                    )
            ),
            ResourceMeta(
                    name = "CWS-HANLP",
                    version = "1.0.0",
                    fileName = "mynlp-resource-cws-hanlp.jar",
                    content = listOf(
                            "cws-hanlp-model/feature.dat",
                            "cws-hanlp-model/feature.txt",
                            "cws-hanlp-model/parameter.bin"
                    )
            )
    )

    private val mapIndex = HashMap<String, ResourceMeta>()

    private val lock = Any()

    init {
        metaList.forEach { x ->
            x.content.forEach { y ->
                mapIndex[y] = x
            }
        }
    }
}

data class ResourceMeta(
        val name: String,
        val version: String,
        val fileName: String,
        val content: List<String>) {

    fun fileNameWithVersion() = fileName.replace(".jar", "-${version}.jar")
}
