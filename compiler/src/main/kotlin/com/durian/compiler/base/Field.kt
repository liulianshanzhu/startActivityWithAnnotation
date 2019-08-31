package com.durian.compiler.base

import com.durian.compiler.utils.asJavaTypeName
import com.durian.compiler.utils.asKotlinTypeName
import com.sun.tools.javac.code.Symbol

/**
 * 说明：必填属性的基本信息封装类
 * @author 黄志敏
 * @since 2019/8/31
 */
open class Field(private val symbol: Symbol.VarSymbol): Comparable<Field> {
    val name = symbol.qualifiedName.toString()

    val isPrimitive = symbol.type.isPrimitive

    val isPrivate = symbol.isPrivate

    fun asJavaTypeName() = symbol.type.asJavaTypeName()

    open fun asKotlinTypeName() = symbol.type.asKotlinTypeName()

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }

}