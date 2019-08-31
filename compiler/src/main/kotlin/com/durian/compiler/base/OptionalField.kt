package com.durian.compiler.base

import com.sun.tools.javac.code.Symbol

/**
 * 说明：选填参数的属性封装类
 * @author 黄志敏
 * @since 2019/8/31
 */
class OptionalField(symbol: Symbol.VarSymbol): Field(symbol) {

    /**
     * 其kotlin属性为可空类型
     */
    override fun asKotlinTypeName() = super.asKotlinTypeName().asNullable()
}