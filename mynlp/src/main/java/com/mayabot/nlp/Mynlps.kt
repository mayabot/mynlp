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
import java.util.function.Consumer

/**
 * Mynlps 单例对象。默认提供一个全局单例mynlp对象
 *
 * @author jimichan
 */
@Deprecated("Use Mynlp")
object Mynlps {

    @JvmStatic
    @Deprecated("")
    val logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps")!!

    /**
     * 其他任何Mynlp方式之前调用，通过回调MynlpBuilder进行系统设置。
     * @param consumer 设置MynlpBuilder
     */
    @JvmStatic
    @Deprecated("")
    fun install(consumer: Consumer<MynlpBuilder>) {
        Mynlp.init {
            consumer.accept(it)
        }
    }

    @Deprecated("")
    @JvmStatic
    fun config(consumer: Consumer<MynlpBuilder>) {
        Mynlp.init {
            consumer.accept(it)
        }
    }

    @Deprecated("")
    fun install(consumer: (MynlpBuilder) -> Unit) {
        Mynlp.init {
            consumer(it)
        }
    }

    @Deprecated("")
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
    @Deprecated("")
    fun setDataDir(dataDir: String) {
        Mynlp.setDataDir(dataDir)
    }

    @JvmStatic
    @Deprecated("")
    fun setCacheDir(dir: String) {
        Mynlp.setCacheDir(dir)
    }

    @JvmStatic
    @Deprecated("")
    fun set(settingItem: SettingItem<*>, value: String) {
        install { it.set(settingItem, value) }
    }

    @JvmStatic
    @Deprecated("")
    fun set(key: String, value: String) {
        install { it.set(key, value) }
    }

    /**
     * 获取全局唯一的Mynlp实例。
     * @return Mynlp
     */
    @JvmStatic
    @Deprecated(message = "use mynlp", replaceWith = ReplaceWith("Mynlp.singleton()"))
    fun get(): Mynlp {
        return Mynlp.singleton()
    }


    /**
     * 返回Mynlp容器中指定class的Bean。
     *
     * @param clazz class
     * @param <T>   类型参数
     * @return 返回实例Bean
    </T> */
    @JvmStatic
    @Deprecated("")
    fun <T> instanceOf(clazz: Class<T>): T {
        return Mynlp.getInstance(clazz)
    }

    @Deprecated("")
    inline fun <reified T> instanceOf(): T {
        return Mynlp.getInstance(T::class.java)
    }

}
