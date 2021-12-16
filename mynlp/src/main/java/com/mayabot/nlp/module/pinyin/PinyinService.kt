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

package com.mayabot.nlp.module.pinyin

import com.mayabot.nlp.MynlpConfigs.pinyinExtDicSetting
import com.mayabot.nlp.MynlpConfigs.pinyinSetting
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.common.injector.Singleton
import com.mayabot.nlp.common.logging.InternalLoggerFactory
import com.mayabot.nlp.common.resources.NlpResource
import com.mayabot.nlp.module.pinyin.Tex2PinyinComputer.parse
import com.mayabot.nlp.module.pinyin.model.Pinyin
import com.mayabot.nlp.module.pinyin.model.SimplePinyin
import java.util.*

/**
 * 单例保存系统内置的拼音表
 */
@Singleton
class PinyinInnerDict(private val env: MynlpEnv) {

    companion object {
        val logger = InternalLoggerFactory.getInstance(PinyinInnerDict::class.java)
    }

    val pinyinTable = load()

    private val charPinyin: Array<Array<SimplePinyin>?>


    init {
        // 计算所有单字的拼音。也就是说自定义拼音只能指定词组的拼音
        // 统计单子的拼音
        var max = -1
        for (s in pinyinTable.keys) {
            if (s.length == 1) {
                val c = s[0].code
                if (c > max) {
                    max = c
                }
            }
        }

        charPinyin = arrayOfNulls(max + 1)

        pinyinTable.entries.forEach { (key, value) ->
            if (key.length == 1) {
                charPinyin[(key[0].code)] = value.map { it.simple }.toTypedArray()
            }
        }
    }

    fun charPinyin(char: Char): Array<SimplePinyin>? {
        return charPinyin[char.code]
    }

    private fun load(): Map<String, Array<Pinyin>> {
        // 3万行，463k
        logger.debug("开始加载内置拼音表资源")
        val list = ArrayList<NlpResource?>()

        list.add(env.loadResource(env.get(pinyinSetting)))

        val ext = env.tryLoadResource(pinyinExtDicSetting)
        if (ext != null) {
            list.add(ext)
        }

        val map = TreeMap<String, Array<Pinyin>>()
        for (dictResource in list.filterNotNull()) {
            dictResource.inputStream()
                .bufferedReader()
                .forEachLine { line ->
                    //降龙伏虎=xiang2,long2,fu2,hu3
                    //单=dan1,shan4,chan2
                    val param = line.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    val key = param[0]
                    try {
                        val pinyins = parse(param[1])
                        if (pinyins != null) {
                            map[key] = pinyins
                        }
                    } catch (e: java.lang.Exception) {
                        logger.error("parse ${param[1]}", e)
                    }
                }
        }

        return map
    }
}

/**
 * 拼音的词典
 *
 * @author jimichan
 */
@Singleton
class PinyinService constructor(private val innerDict: PinyinInnerDict) {

    val customPinyin = CustomPinyin()

    private val computer = Tex2PinyinComputer(innerDict, Tex2PinyinComputer.convert(customPinyin.map))

    fun rebuild() {
        computer.setUpCustomPinyin(customPinyin.map)
    }

    fun convert(text: String): PinyinResult {
        return computer.convert(text)
    }

    fun charPinyin(ch: Char): Array<SimplePinyin>? {
        return innerDict.charPinyin(ch)
    }


}
