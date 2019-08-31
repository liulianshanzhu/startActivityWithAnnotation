package com.durian.compiler.android

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/26
 */

object Java {
    val ACTIVITY = com.squareup.javapoet.ClassName.get("android.app", "Activity")
    val FRAGMENT = com.squareup.javapoet.ClassName.get("androidx.fragment.app", "Fragment")
    val VIEW = com.squareup.javapoet.ClassName.get("android.view", "View")
    val CONTEXT = com.squareup.javapoet.ClassName.get("android.content", "Context")
    val INTENT = com.squareup.javapoet.ClassName.get("android.content", "Intent")
    val BUNDLE = com.squareup.javapoet.ClassName.get("android.os", "Bundle")
    val BOOLEAN = com.squareup.javapoet.ClassName.get("java.lang", "Boolean")
    val NON_NULL = com.squareup.javapoet.ClassName.get("androidx.annotation", "NonNull")
    val NULLABLE = com.squareup.javapoet.ClassName.get("androidx.annotation", "Nullable")
    val STRING = com.squareup.javapoet.ClassName.get("java.lang", "String")
}

object Kotlin {
    @JvmField
    val STRING: ClassName = ClassName("kotlin", "String")
    @JvmField
    val STRING_NULLABLE: ClassName = ClassName("kotlin", "String?")
    @JvmField
    val STRING_ARRAY = ClassName("kotlin", "Array").parameterizedBy(STRING)
    @JvmField
    val LONG_ARRAY: ClassName = ClassName("kotlin", "LongArray")
    @JvmField
    val INT_ARRAY: ClassName = ClassName("kotlin", "IntArray")
    @JvmField
    val SHORT_ARRAY: ClassName = ClassName("kotlin", "ShortArray")
    @JvmField
    val BYTE_ARRAY: ClassName = ClassName("kotlin", "ByteArray")
    @JvmField
    val CHAR_ARRAY: ClassName = ClassName("kotlin", "CharArray")
    @JvmField
    val BOOLEAN_ARRAY: ClassName = ClassName("kotlin", "BooleanArray")
    @JvmField
    val FLOAT_ARRAY: ClassName = ClassName("kotlin", "FloatArray")
    @JvmField
    val DOUBLE_ARRAY: ClassName = ClassName("kotlin", "DoubleArray")
    @JvmField
    val ACTIVITY = ClassName("android.app", "Activity")
    @JvmField
    val FRAGMENT = ClassName("androidx.fragment.app", "Fragment")
    @JvmField
    val SERVICE = ClassName("android.app", "Service")
    @JvmField
    val VIEW = ClassName("android.view", "View")
    @JvmField
    val CONTEXT = ClassName("android.content", "Context")
    @JvmField
    val INTENT = ClassName("android.content", "Intent")
    @JvmField
    val BUNDLE = ClassName("android.os", "Bundle")
    @JvmField
    val LOG = ClassName("android.util", "Log")
}

object AndroidName {
    @JvmField
    val ACTIVITY = "android.app.Activity"
    @JvmField
    val FRAGMENT = "android.app.Fragment"
    @JvmField
    val FRAGMENT_V4 = "androidx.fragment.app.Fragment"
    @JvmField
    val SERVICE = "android.app.Service"
    @JvmField
    val PARCELABLE = "android.os.Parcelable"
}
