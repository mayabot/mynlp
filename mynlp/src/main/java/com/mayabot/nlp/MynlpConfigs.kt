package com.mayabot.nlp

import com.mayabot.nlp.common.SettingItem
import com.mayabot.nlp.common.SettingItem.stringSetting

object MynlpConfigs {

    @JvmField
    val server: SettingItem<String> = stringSetting("mynlp.server", "")

    @JvmField
    val cwsModelItem: SettingItem<String> = stringSetting("cws.model", "cws-model")

    /**
     * value可以是用逗号分隔的多个值
     */
    @JvmField
    val dictPathSetting: SettingItem<String> = stringSetting(
            "custom.dictionary.path", "custom-dict/CustomDictionary.txt")

    @JvmField
    val pinyinSetting: SettingItem<String> = stringSetting("pinyin.dict", "mynlp-pinyin.txt")

    @JvmField
    val pinyinExtDicSetting: SettingItem<String> = stringSetting("pinyin.ext.dict", null)

    /**
     * 分词纠错词典配置
     */
    @JvmField
    val correctionDict: SettingItem<String> = stringSetting("correction.dict", "dictionary/correction.txt")
}