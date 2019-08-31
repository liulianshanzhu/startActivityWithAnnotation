package com.durian.compiler.base

import com.durian.compiler.builder.JavaBuilder
import com.durian.compiler.builder.KotlinBuilder
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31
 */
open class ClassBuilder(val classInfo: ClassInfo) {
    private lateinit var filer: Filer
    private val javaBuilder = JavaBuilder()
    private val kotlinBuilder = KotlinBuilder()

    fun build(filer: Filer) {
        this.filer = filer
        if (classInfo.isKotlin) {
            brewKotlin(kotlinBuilder.build(classInfo))
        }
        brewJava(javaBuilder.build(classInfo))
    }

    fun brewJava(typeSpec: TypeSpec) {
        val builder = JavaFile.builder(classInfo.packageName, typeSpec)
        val builderFile = builder.build()
        builderFile.writeTo(filer)
    }

    fun brewKotlin(fileSpec: FileSpec) {
        try {
            val fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, classInfo.packageName, fileSpec.name + ".kt")
            fileObject.openWriter()
                .also(fileSpec::writeTo)
                .close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}