package com.mayabot.nlp.segment

import com.google.inject.Binder
import com.google.inject.Module
import com.mayabot.nlp.MynlpEnv
import java.io.File
import java.util.regex.Pattern

class SegmentModule(private val env: MynlpEnv) : Module {

    private val metaList = ArrayList<ResourceMeta>()

    private val mapIndex = HashMap<String, ResourceMeta>()

    private val lock = Any()

    init {

        metaList += ResourceMeta(
                name = "CoreDict",
                version = "1.0.0",
                fileName = "mynlp-resource-coredict.jar",
                content = listOf("core-dict/CoreDict.txt", "core-dict/CoreDict.bigram.txt")
        )

        metaList += ResourceMeta(
                name = "POS",
                version = "1.0.0",
                fileName = "mynlp-resource-pos.jar",
                content = listOf("pos-model/feature.txt", "pos-model/label.txt", "pos-model/parameter.bin")
        )

        metaList += ResourceMeta(
                name = "NER",
                version = "1.0.0",
                fileName = "mynlp-resource-ner.jar",
                content = listOf(
                        "ner-model/feature.txt", "ner-model/label.txt", "ner-model/parameter.bin",
                        "person-name-model/feature.txt",
                        "person-name-model/parameter.bin"
                )
        )

        metaList += ResourceMeta(
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
        )

        metaList += ResourceMeta(
                name = "STOPWORD",
                version = "1.0.0",
                fileName = "mynlp-resource-stopword.jar",
                content = listOf(
                        "stopword-dict/stopwords.txt"
                )
        )

        metaList += ResourceMeta(
                name = "CWS",
                version = "1.0.0",
                fileName = "mynlp-resource-cws.jar",
                content = listOf(
                        "cws-model/feature.dat",
                        "cws-model/feature.txt",
                        "cws-model/parameter.bin"
                )
        )

        metaList += ResourceMeta(
                name = "CWS-HANLP",
                version = "1.0.0",
                fileName = "mynlp-resource-cws-hanlp.jar",
                content = listOf(
                        "cws-hanlp-model/feature.dat",
                        "cws-hanlp-model/feature.txt",
                        "cws-hanlp-model/parameter.bin"
                )
        )

        metaList.forEach { x ->
            x.content.forEach { y ->
                mapIndex[y] = x
            }
        }
    }

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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val pattern = Pattern.compile("^(.*?)-(\\d+[\\.\\d]+)\\.jar$")
            val matcher = pattern.matcher("mynlp-resource-cws-1.0.0.jar")
            while (matcher.find()) {
                println(matcher.group(1) + "   " + matcher.group(2))
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
}

data class ResourceMeta(
        val name: String,
        val version: String,
        val fileName: String,
        val content: List<String>) {

    fun fileNameWithVersion() = fileName.replace(".jar", "-${version}.jar")
}
