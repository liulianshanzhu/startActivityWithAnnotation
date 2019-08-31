package com.durian.annotationdemo.utils

import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/26 15:50
 */

object ReflectUtil {

    /**
     * 通过反射获取类的对象, 只通过public的构造方法获取
     *
     *
     * 通过循环的方式构造对象, 因为构造函数的数量还是非常小的, 不会增加多少开销
     * 无法通过传统的方式封装此方法, 因为需要传两个多变参数
     * ([Class.getConstructor] 和 [Constructor.newInstance]都需要)
     * 而且不能通过Object的getClass获取到对应构造里需要的参数class而导致无法匹配
     *
     *
     * @param clz  目标类
     * @param args 构造参数
     * @param <T>  任意类型
     * @return
    </T> */
    fun <T> newInst(clz: Class<T>?, vararg args: Any?): T? {
        if (clz == null) {
            return null
        }

        var t: T? = null
        val cs = clz.constructors
        for (i in cs.indices) {
            try {
                @Suppress("UNCHECKED_CAST")
                t = cs[i].newInstance(*args) as T
                break
            } catch (e: Exception) {
                continue
            }

        }

        return t
    }

    fun <T : Any> newInst(clz: KClass<T>, vararg args: Any?): T? {
        return newInst(clz.java, *args)
    }


    /**
     * 通过反射获取类的对象, 任意构造方法获取, 包括private
     *
     * @param clz
     * @param args
     * @param <T>
     * @return
    </T> */
    fun <T> newDeclaredInst(clz: Class<T>?, vararg args: Any): T? {
        if (clz == null) {
            return null
        }

        var t: T? = null
        val cs = clz.declaredConstructors
        for (i in cs.indices) {
            try {
                cs[i].isAccessible = true
                @Suppress("UNCHECKED_CAST")
                t = cs[i].newInstance(*args) as T
                break
            } catch (e: Exception) {
                continue
            }

        }
        return t
    }

    @Throws(ClassNotFoundException::class, NoSuchMethodException::class)
    fun getMethod(className: String, methodName: String, vararg parameterTypes: Class<*>): Method {
        return Class.forName(className).getMethod(methodName, *parameterTypes)
    }

}
