package com.durian.compiler.processor

import com.durian.annotation.Creator
import com.durian.annotation.Extra
import com.durian.compiler.base.ClassInfo
import com.durian.compiler.base.Field
import com.durian.compiler.base.OptionalField
import com.durian.compiler.utils.AptContext
import com.google.auto.service.AutoService
import com.sun.tools.javac.code.Symbol
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31
 */
@AutoService(Processor::class)
class CreatorProcessor: BaseProcessor() {

    override fun createFile(env: RoundEnvironment) {
        val classes = HashMap<Element, ClassInfo>()
        env.getElementsAnnotatedWith(Creator::class.java)
            .filter { it.kind.isClass } //避免把注解写在非类上
            .forEach { element: Element ->
                try {
                    classes[element] = ClassInfo(element as TypeElement)
                } catch (e: Exception){
                    AptContext.messager.printMessage(Diagnostic.Kind.ERROR, "Creator classes error:${e.message}")
                }
            }
        env.getElementsAnnotatedWith(Extra::class.java)
            .filter { it.kind == ElementKind.FIELD }
            .forEach { element ->
                val extra = element.getAnnotation(Extra::class.java)
                if (extra.value) {//非必要参数
                    classes[element.enclosingElement]?.addField(OptionalField(element as Symbol.VarSymbol))
                }else {//必要参数
                    classes[element.enclosingElement]?.addField(Field(element as Symbol.VarSymbol))
                }
            }
        classes.values.map(ClassInfo::builder).forEach {
            it.build(AptContext.filer)
        }
    }


}