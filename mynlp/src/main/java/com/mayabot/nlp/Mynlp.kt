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

import com.mayabot.nlp.common.injector.Injector
import com.mayabot.nlp.common.logging.InternalLoggerFactory
import com.mayabot.nlp.common.resources.NlpResource
import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer
import com.mayabot.nlp.segment.Sentence
import java.security.AccessController
import java.security.PrivilegedAction

/**
 * 包含了执行环境和IOC容器
 *
 * @author jimichan
 */
class Mynlp internal constructor(val env: MynlpEnv,
                                 /**
                                  * guice injector
                                  */
                                 private val injector: Injector) {


    fun <T> instance(clazz: Class<T>): T {
        return AccessController.doPrivileged(PrivilegedAction<T> {
            try {
                injector.getInstance(clazz)
            } catch (e: Exception) {
                throw RuntimeException("Mynlp getInstance of $clazz Error!", e)
            }
        })
    }

    inline fun <reified T> instance(): T {
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

    fun loadResource(resourcePath: String?): NlpResource? {
        return env.loadResource(resourcePath)
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
        fun init(initer: MynlpIniter) {
            if (inited) {
                throw RuntimeException("mynlp全局单例已经实例化！")
            }
            AccessController.doPrivileged(PrivilegedAction<Unit> {
                initer.init(builder)
            })
        }

        fun init(initer: (MynlpBuilder) -> Unit) {
            if (inited) {
                throw RuntimeException("mynlp全局单例已经实例化！")
            }
            AccessController.doPrivileged(PrivilegedAction<Unit> {
                initer(builder)
            })
        }

        @JvmStatic
        fun singleton(): Mynlp {
            return mynlp_
        }

        /**
         * 设置DataDir。
         * 在调用install和其他任何Mynlp方式之前调用
         *
         * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
         */
        @JvmStatic
        fun setDataDir(dataDir: String) {
            init { it.dataDir = dataDir }
        }

        @JvmStatic
        fun setCacheDir(dir: String) {
            init { it.cacheDir = dir }
        }

        @JvmStatic
        fun set(settingItem: SettingItem<*>, value: String) {
            init { it.set(settingItem, value) }
        }

        @JvmStatic
        fun set(key: String, value: String) {
            init { it.set(key, value) }
        }

        @JvmStatic
        fun <T> getInstance(clazz: Class<T>): T {
            return singleton().instance(clazz)!!
        }

        inline fun <reified T> getInstance(): T {
            return singleton().instance(T::class.java)
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        @JvmStatic
        fun segment(text: String): Sentence {
            return singleton().bigramLexer().scan(text)
        }


    }
}