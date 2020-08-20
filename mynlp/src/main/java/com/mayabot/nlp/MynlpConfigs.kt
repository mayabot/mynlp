package com.mayabot.nlp

import com.mayabot.nlp.SettingItem.stringSetting

object MynlpConfigs {

    @JvmField
    val server = stringSetting("mynlp.server", "")

    @JvmField
    val cwsModelItem = stringSetting("cws.model", "cws-model")


    /**
     * value可以是用逗号分隔的多个值
     */
    @JvmField
    val dictPathSetting = stringSetting(
            "custom.dictionary.path", "custom-dict/CustomDictionary.txt")


    @JvmField
    val pinyinSetting = stringSetting("pinyin.dict", "mynlp-pinyin.txt")

    @JvmField
    val pinyinExtDicSetting = stringSetting("pinyin.ext.dict", null)

    /**
     * 分词纠错词典配置
     */
    @JvmField
    val correctionDict = stringSetting("correction.dict", "dictionary/correction.txt")
}