package com.mayabot.nlp.common.injector

import org.jetbrains.annotations.Nullable
import java.util.concurrent.ConcurrentHashMap

// 单例标记注解
// 默认实现注解
// 不支持泛型

interface Injector {
    fun <T> getInstance(clazz: Class<T>): T?
    fun <T> getInstance(clazz: Class<T>, tag: String): T?

    fun cache(): ConcurrentHashMap<Class<*>, Any>

    companion object {
        @JvmStatic
        fun create(modules: List<Module> = emptyList()): Injector {
            val mergeMap = ConcurrentHashMap<String, BeanFactory>()
            modules.forEach {
                it.configure()
                val map = it.result()
                map.forEach { (t, u) ->
                    mergeMap[t] = u
                }
            }

            return InjectorImpl(mergeMap)
        }
    }
}

inline fun <reified T> Injector.getInstance() = this.getInstance(T::class.java)

private fun <T> makeKey(clazz: Class<T>, tag: String): String {
    return "${clazz.name}-$tag"
}

@Suppress("UNCHECKED_CAST")
class InjectorImpl(
        private val bindMap: ConcurrentHashMap<String, BeanFactory>
) : Injector {

    private val singletonCache = ConcurrentHashMap<Class<*>, Any>()

    override fun cache() = singletonCache

    override fun <T> getInstance(
            clazz: Class<T>
    ): T? {
        return this.getInstance(clazz, "")
    }

    override fun <T> getInstance(
            clazz: Class<T>,
            tag: String
    ): T? {

        val injector = this

        val key = makeKey(clazz, tag)

        var beanFactory = bindMap[key]

        if (beanFactory != null) {
            if (beanFactory == NULLBeanFactory) {
                return null
            }
            if (beanFactory is InstanceBeanFactory) {
                return beanFactory.obj as T
            }

            return beanFactory.create(this) as T
        }

        if (tag == "") {
            beanFactory = makeAtomBeanFactory(clazz)
        }

        if (beanFactory == null) {
            bindMap[key] = NULLBeanFactory
            return null
        } else {
            bindMap[key] = beanFactory
        }

        return beanFactory.create(injector) as T
    }

    // 关键点在于自动产生
    fun makeAtomBeanFactory(clazz: Class<*>): BeanFactory {
        val ans = clazz.declaredAnnotations
        val defaultImpl: Class<*>? = ans.filterIsInstance(ImplementedBy::class.java).map { it.value.java }.firstOrNull()

        if (clazz.isInterface && defaultImpl == null) {
            return NULLBeanFactory
        }

        val targetClass = if (clazz.isInterface) defaultImpl!! else clazz

        val bf = TargetClassFactory(targetClass)

        return bf
    }

}

val NULLBeanFactory = InstanceBeanFactory(Unit)




class InstanceBeanFactory(val obj: Any) : BeanFactory {
    override fun create(injector: Injector): Any {
        return obj
    }
}

/**
 * 用反射自动创建一个Class的工厂
 */
class TargetClassFactory(val clazz: Class<*>) : BeanFactory {

    val constructors = clazz.declaredConstructors

    var singleton = clazz.declaredAnnotations.any { it is Singleton }

    init {
        if (clazz.isInterface) {
            throw java.lang.RuntimeException("$clazz must not be interface")
        }

        if (constructors.size > 1) {
            throw RuntimeException("${clazz} has more Constructors")
        }
    }


    override fun create(injector: Injector): Any {


        if (singleton) {
            val singletonCache = injector.cache()
            val x = singletonCache[clazz]
            if (x == null) {
                val c2 = create2(injector)
                singletonCache[clazz] = c2
                return c2
            } else {
                return x
            }
        }else{
            return create2(injector)
        }
    }

    private fun create2(injector: Injector): Any {
        println("Create for class " + clazz)
        // class，如果存在唯一一个构造函数
        // 如果构造函数里面的参数是Nullable的，那么autoBind=false

        if (constructors.isEmpty()) {
            return clazz.newInstance()!!
        } else {
            val constructor = constructors.first()

            val count = constructor.parameterCount
            val parameterTypes = constructor.parameterTypes
            val parameterAnnotations = constructor.parameterAnnotations

            if (count == 0) {
                return constructor.newInstance()
            }

            val varpars = Array<Any?>(count) { null }

            for (i in 0 until count) {
                val pc = parameterTypes[i]
                val nullAble = parameterAnnotations[i].any { it is Nullable }
                val obj = injector.getInstance(pc)

                varpars[i] = obj

                if (!nullAble && obj == null) {
                    throw RuntimeException("Init $clazz ,$pc is null")
                }

            }

            return constructor.newInstance(*varpars)
        }
    }
}

interface Module {
    fun configure()
    fun result(): Map<String, BeanFactory>
}

fun createModule(block: AbstractModule.() -> Unit): Module {
    return object : AbstractModule() {
        override fun configure() {
            this.block()
        }
    }
}

abstract class AbstractModule : Module {

    private val bindMap = HashMap<String, BeanFactory>()

    fun <T, X : T> bind(clazz: Class<T>): BindCallback<X> {
        return BindCallback(makeKey(clazz, ""))
    }

    fun <T, X : T> bind(clazz: Class<T>, tag: String): BindCallback<X> {
        return BindCallback(makeKey(clazz, tag))
    }

    override fun result() = bindMap

    inner class BindCallback<in X>(
            private val key: String) {

        fun toInstance(obj: X) {
            bindMap[key] = InstanceBeanFactory(obj as Any)
        }

        fun toClass(clazz: Class<*>) {
            bindMap[key] = TargetClassFactory(clazz)
        }
    }
}
