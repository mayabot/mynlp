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

import com.mayabot.nlp.logging.InternalLoggerFactory
import java.lang.RuntimeException

import java.security.AccessController
import java.security.PrivilegedAction
import java.util.function.Consumer


/**
 * Mynlp包含一个guice实现的IOC容器，管理Mynlp所有的资源。
 * 在项目Mynlp对象应该作为单例，不需要重复创建。
 *
 * @author jimichan
 */
object Mynlps {

    @JvmStatic
    val logger = InternalLoggerFactory.getInstance("com.mayabot.nlp.Mynlps")!!

    private val initList = arrayListOf<Consumer<MynlpBuilder>>()

    private var inited = false

    private val mynlp:Mynlp by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        inited = true
        val builder = MynlpBuilder()

        initList.forEach {
            it.accept(builder)
        }

         AccessController.doPrivileged(PrivilegedAction<Mynlp>{
             builder.build()
        })

    }

    /**
     * 其他任何Mynlp方式之前调用，通过回调MynlpBuilder进行系统设置。
     * @param consumer 设置MynlpBuilder
     */
    @JvmStatic
    fun install(consumer: Consumer<MynlpBuilder>){
        if (inited) {
            throw RuntimeException("必须在调用Mynlp.get之前调用")
        }
        initList+=consumer
    }

    fun install(consumer: (MynlpBuilder)->Unit){
        initList += Consumer<MynlpBuilder> {
            consumer(it)
        }
    }

    /**
     * 设置DataDir。
     * 在调用install和其他任何Mynlp方式之前调用
     *
     * @param dataDir 数据目录。默认在当前用户目录下.mynlp.data文件夹
     */
    @JvmStatic
    fun setDataDir(dataDir: String) {
        if (inited) {
            throw RuntimeException("必须在调用Mynlp.get之前调用")
        }
        AccessController.doPrivileged(PrivilegedAction<Unit>{
            System.setProperty("mynlp.data.dir", dataDir)
        })
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
        return AccessController.doPrivileged(PrivilegedAction<T>{
            mynlp.getInstance(clazz)
        })
    }

    inline fun <reified T> instanceOf():T{
        return instanceOf(T::class.java)
    }

}
