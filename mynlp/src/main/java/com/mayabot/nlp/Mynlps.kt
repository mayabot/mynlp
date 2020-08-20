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

import com.mayabot.nlp.common.logging.InternalLoggerFactory
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.*
import java.util.function.Consumer


/**
 * Mynlps 单例对象。默认提供一个全局单例mynlp对象
 *
 * @author jimichan
 */
object Mynlps {

    @JvmStatic
    val logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps")!!

    private val initList = arrayListOf<Consumer<MynlpBuilder>>()

    private var inited = false

    private val mynlp: Mynlp by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        inited = true
        createMynlp()
    }

    private fun createMynlp(): Mynlp {
        val builder = MynlpBuilder()

        AccessController.doPrivileged(PrivilegedAction<Unit> {
            initList.forEach {
                it.accept(builder)
            }
        })

        return builder.build()
    }

    /**
     * 其他任何Mynlp方式之前调用，通过回调MynlpBuilder进行系统设置。
     * @param consumer 设置MynlpBuilder
     */
    @JvmStatic
    fun install(consumer: Consumer<MynlpBuilder>) {
        if (inited) {
            throw RuntimeException("必须在调用Mynlp.get之前调用")
        }
        initList += consumer
    }

    @JvmStatic
    fun config(consumer: Consumer<MynlpBuilder>) {
        this.install(consumer)
    }

    fun install(consumer: (MynlpBuilder) -> Unit) {
        if (inited) {
            throw RuntimeException("必须在调用Mynlp.get之前调用")
        }
        initList += Consumer<MynlpBuilder> {
            consumer(it)
        }
    }

    fun config(consumer: (MynlpBuilder) -> Unit) {
        this.install(consumer)
    }

    /**
     * 设置DataDir。
     * 在调用install和其他任何Mynlp方式之前调用
     *
     * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
     */
    @JvmStatic
    fun setDataDir(dataDir: String) {
        install { it.dataDir = dataDir }
    }

    @JvmStatic
    fun setCacheDir(dir: String) {
        install { it.cacheDir = dir }
    }

    @JvmStatic
    fun set(settingItem: SettingItem<*>, value: String) {
        install { it.set(settingItem, value) }
    }

    @JvmStatic
    fun set(key: String, value: String) {
        install { it.set(key, value) }
    }

    @JvmStatic
    fun loadSettingFromProperties(properties: Properties) {
        install {
            properties.keys.forEach { key ->
                val value = properties.getProperty(key.toString())!!
                if (key.toString().isNotBlank() && value.isNotBlank()) {
                    it.set(key.toString(), value)
                }
            }
        }
    }

    /**
     * 获取全局唯一的Mynlp实例。
     * @return Mynlp
     */
    @JvmStatic
    fun get(): Mynlp {
        return mynlp
    }

    /**
     * 返回Mynlp容器中指定class的Bean。
     *
     * @param clazz class
     * @param <T>   类型参数
     * @return 返回实例Bean
    </T> */
    @JvmStatic
    fun <T> instanceOf(clazz: Class<T>): T {
        return AccessController.doPrivileged(PrivilegedAction<T> {
            mynlp.getInstance(clazz)
        })
    }

    inline fun <reified T> instanceOf(): T {
        return instanceOf(T::class.java)
    }

}

inline fun <reified T> Mynlp.getInstance(): T {
    return this.getInstance(T::class.java)
}
