package com.durian.compiler.processor

import com.durian.annotation.Creator
import com.durian.annotation.Extra
import com.durian.compiler.utils.AptContext
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/26
 */
abstract class BaseProcessor : AbstractProcessor() {
    private val supportedAnnotations = setOf(Creator::class.java, Extra::class.java)

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        AptContext.init(env)
    }

    override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
        createFile(env)
        return false
    }

    abstract fun createFile(env: RoundEnvironment)

    override fun getSupportedAnnotationTypes() = supportedAnnotations.mapTo(HashSet<String>(), Class<*>::getCanonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()

}
