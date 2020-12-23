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
package com.mayabot.nlp

import com.mayabot.nlp.common.SettingItem
import com.mayabot.nlp.common.injector.Injector
import com.mayabot.nlp.common.logging.InternalLoggerFactory
import com.mayabot.nlp.common.resources.NlpResource
import com.mayabot.nlp.module.pinyin.PinyinService
import com.mayabot.nlp.module.pinyin.split.PinyinSplitService
import com.mayabot.nlp.module.summary.KeywordSummary
import com.mayabot.nlp.module.summary.SentenceSummary
import com.mayabot.nlp.module.trans.Simplified2Traditional
import com.mayabot.nlp.module.trans.Traditional2Simplified
import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.Lexers
import com.mayabot.nlp.segment.Sentence
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.function.Consumer

/**
 * 包含了执行环境和IOC容器
 *
 * @author jimichan
 */
class Mynlp internal
constructor(
        val env: MynlpEnv,
        /**
         * guice injector
         */
        private val injector: Injector) {


    fun <T> getInstance(clazz: Class<T>): T {
        return AccessController.doPrivileged(PrivilegedAction<T> {
            try {
                injector.getInstance(clazz)
            } catch (e: Exception) {
                throw RuntimeException("Mynlp getInstance of $clazz Error!", e)
            }
        })
    }

    inline fun <reified T> getInstance(): T {
        return getInstance(T::class.java)
    }

    fun lexerBuilder(): FluentLexerBuilder {
        return FluentLexerBuilder(this)
    }

    fun bigramLexer(): Lexer {
        return lexerBuilder()
                .bigram()
                .withPos()
                .withPersonName().build()
    }

    fun perceptronLexer(): Lexer {
        return lexerBuilder().perceptron().withPos().build()
    }

    private val pinyin: PinyinService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        getInstance(PinyinService::class.java)
    }

    /**
     * 拼音服务
     */
    fun pinyin(): PinyinService {
        return pinyin
    }

    fun convertPinyin(text: String) = pinyin.convert(text)

    fun loadResource(resourcePath: String?): NlpResource? {
        return env.loadResource(resourcePath)
    }

    private val pinyinSplit: PinyinSplitService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        getInstance(PinyinSplitService::class.java)
    }

    /**
     * [text]连续的拼音流
     */
    fun splitPinyin(text: String): List<String> {
        return pinyinSplit.split(text)
    }


    /**
     * 便捷的分词方法，使用bigram分词器
     */
    fun segment(text: String): Sentence {
        return bigramLexer().scan(text)
    }

    private val s2t: Simplified2Traditional by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        getInstance(Simplified2Traditional::class.java)
    }

    private val t2s: Traditional2Simplified by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        getInstance(Traditional2Simplified::class.java)
    }

    /**
     * 简体转繁体
     *
     * @param text 简体文字
     * @return 繁体文字
     */
    fun s2t(text: String): String {
        return s2t.transform(text)
    }

    /**
     * 繁体转简体
     *
     * @param text 繁体内容
     * @return 简体字符串
     */
    fun t2s(text: String): String {
        return t2s.transform(text)
    }

    /**
     * 简单句子摘要实现
     * SentenceSummary 对象可重用
     */
    fun sentenceSummary(): SentenceSummary {
        val lexer = bigramLexer().filterReader(true, true)
        return SentenceSummary(lexer)
    }

    fun keywordSummary(): KeywordSummary {
        val lexer = Lexers.core().filterReader(true, true)
        return KeywordSummary(lexer)
    }

    interface CommonConfig {
        /**
         * 设置DataDir。
         * 在调用install和其他任何Mynlp方式之前调用
         *
         * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
         */
        fun setDataDir(dataDir: String): CommonConfig

        fun setCacheDir(dir: String): CommonConfig

        fun setAutoDownloadRes(isAuto: Boolean): CommonConfig

        fun set(settingItem: SettingItem<*>, value: String): CommonConfig

        fun set(key: String, value: String): CommonConfig
    }

    companion object {

        @JvmField
        val logger = InternalLoggerFactory.getInstance(Mynlp::class.java)!!

        private val builder = MynlpBuilder()

        private var inited = false

        private val mynlp_: Mynlp by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            inited = true
            builder.build()
        }

        @JvmStatic
        fun config(callback: Consumer<MynlpBuilder>) {
            if (inited) {
                throw RuntimeException("mynlp全局单例已经实例化！")
            }
            AccessController.doPrivileged(PrivilegedAction<Unit> {
                callback.accept(builder)
            })
        }

        @JvmStatic
        fun configer(): CommonConfig {
            if (inited) {
                throw RuntimeException("mynlp全局单例已经实例化！")
            }
            return object : CommonConfig {
                /**
                 * 设置DataDir。
                 * 在调用install和其他任何Mynlp方式之前调用
                 *
                 * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
                 */
                override fun setDataDir(dataDir: String): CommonConfig {
                    this@Companion.config(Consumer {
                        it.dataDir = dataDir
                    })
                    return this
                }

                override fun setCacheDir(dir: String): CommonConfig {
                    this@Companion.config(Consumer {
                        it.cacheDir = dir
                    })
                    return this
                }

                override fun setAutoDownloadRes(isAuto: Boolean): CommonConfig {
                    this@Companion.config(Consumer {
                        it.setAutoDownloadResource(isAuto)
                    })
                    return this
                }

                override fun set(settingItem: SettingItem<*>, value: String): CommonConfig {
                    this@Companion.config(Consumer {
                        it.set(settingItem, value)
                    })
                    return this
                }

                override fun set(key: String, value: String): CommonConfig {
                    this@Companion.config(Consumer {
                        it.set(key, value)
                    })
                    return this
                }
            }
        }

        @JvmStatic
        fun instance(): Mynlp {
            return mynlp_
        }

    }
}