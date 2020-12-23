package com.mayabot.nlp.common.utils

/**
 *
 */
object DictResources {

    val list: List<DictResDesc> = init()

    val map = list.flatMap { desc ->
        desc.resources.map { it to desc }
    }.toMap()

    fun init(): List<DictResDesc> {
        val list = ArrayList<DictResDesc>()

        val group = "com.mayabot.mynlp.resource"

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-coredict",
            version = "1.0.0",
            resources = listOf(
                "core-dict/CoreDict.bigram.txt",
                "core-dict/CoreDict.txt"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-pos",
            version = "1.0.0",
            resources = listOf("pos-model/feature.dat",
                "pos-model/feature.txt",
                "pos-model/label.txt",
                "pos-model/parameter.bin"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-ner",
            version = "1.0.0",
            resources = listOf(
                "ner-model/feature.txt",
                "ner-model/label.txt",
                "ner-model/parameter.bin",
                "person-name-model/feature.txt",
                "person-name-model/parameter.bin"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-pinyin",
            version = "1.1.0",
            resources = listOf(
                "mynlp-pinyin.txt",
                "pinyin-split-model/feature.txt",
                "pinyin-split-model/parameter.bin"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-transform",
            version = "1.0.0",
            resources = listOf(
                "ts-dict/s2t.txt",
                "ts-dict/t2s.txt"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-cws",
            version = "1.0.0",
            resources = listOf(
                "cws-model/feature.dat",
                "cws-model/feature.txt",
                "cws-model/parameter.bin"
            )
        )

        list += DictResDesc(
            group = group,
            artifactId = "mynlp-resource-custom",
            version = "1.0.0",
            resources = listOf(
                "custom-dict/CustomDictionary.txt",
                "custom-dict/上海地名.txt",
                "custom-dict/人名词典.txt",
                "custom-dict/全国地名大全.txt",
                "custom-dict/机构名词典.txt",
                "custom-dict/现代汉语补充词库.txt"
            )
        )

        return list
    }
}

data class DictResDesc(
        val group: String,
        val artifactId: String,
        val version: String,
        val resources: List<String>
)