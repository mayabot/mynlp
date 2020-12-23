package com.mayabot.nlp

import com.mayabot.nlp.common.SettingItem
import com.mayabot.nlp.common.SettingItem.stringSetting

object MynlpConfigs {

    @JvmField
    val server: SettingItem<String> = stringSetting("mynlp.server", "")

    /**
     * AP分词器的模型名
     */
    @JvmField
    val cwsModelItem: SettingItem<String> = stringSetting("cws.model", "cws-model")

    /**
     * 自定义词典的路径
     * value可以是用逗号分隔的多个值，表示多个文件
     */
    @JvmField
    val dictPathSetting: SettingItem<String> = stringSetting(
        "custom.dictionary.path", "custom-dict/CustomDictionary.txt"
    )

    /**
     * 主要拼音的资源文件名
     */
    @JvmField
    val pinyinSetting: SettingItem<String> = stringSetting("pinyin.dict", "mynlp-pinyin.txt")

    /**
     * 拼音自定义扩展词典的文件名（可选）
     */
    @JvmField
    val pinyinExtDicSetting: SettingItem<String> = stringSetting("pinyin.ext.dict", null)

    /**
     * 分词纠错词典配置
     */
    @JvmField
    val correctionDict: SettingItem<String> = stringSetting("correction.dict", "dictionary/correction.txt")
}