package com.durian.compiler.utils

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * 说明：由于在不同地方需要使用到以下各类属性，因此把它抽取出来，并在BaseProcessorNewKt.init()初始化
 * @author 黄志敏
 * @since 2019/8/31
 */
object AptContext {
    lateinit var types: Types
    lateinit var elements: Elements
    lateinit var messager: Messager
    lateinit var filer: Filer

    fun init(env: ProcessingEnvironment){
        elements = env.elementUtils
        types = env.typeUtils
        messager = env.messager
        filer = env.filer
    }
}