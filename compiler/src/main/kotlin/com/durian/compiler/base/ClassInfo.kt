package com.durian.compiler.base

import com.durian.compiler.android.AndroidName
import com.durian.compiler.android.Java
import com.durian.compiler.utils.getTypeFromClassName
import com.durian.compiler.utils.isSubtype
import com.durian.compiler.utils.packageName
import java.util.*
import javax.lang.model.element.TypeElement

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31
 */
class ClassInfo(val typeElement: TypeElement) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        val META_DATA = Class.forName("kotlin.Metadata") as Class<Annotation>
        const val Creator = "Creator"
    }

    val isActivity by lazy {
        typeElement.asType().isSubtype(getTypeFromClassName(AndroidName.ACTIVITY))
    }

    val isFragment by lazy {
        typeElement.asType().isSubtype(getTypeFromClassName(AndroidName.FRAGMENT_V4))
    }

    val isService by lazy {
        typeElement.asType().isSubtype(getTypeFromClassName(AndroidName.SERVICE))
    }

    val simpleName = typeElement.simpleName.toString()

    val packageName: String = typeElement.packageName()

    val isKotlin = typeElement.getAnnotation(META_DATA) != null

    val fields = TreeSet<Field>()

    val required by lazy {
        fields.groupBy { it is OptionalField }[false]?: emptyList()
    }

    val optional by lazy {
        fields.groupBy { it is OptionalField }[true]?: emptyList()
    }

    val instanceType by lazy {
        if (isActivity) Java.ACTIVITY else Java.FRAGMENT
    }


    val builder = ClassBuilder(this)

    fun addField(field: Field) {
        fields.add(field)
    }

}