package com.durian.compiler.utils

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/26 18:18
 */
import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

fun TypeElement.getTypeName() = TypeName.get(this.asType())

/**
 * 获取java的数据类型
 */
fun TypeMirror.asJavaTypeName() = TypeName.get(this).box()

/**
 * 转化为kotlin数据类型
 */
fun TypeMirror.asKotlinTypeName(): com.squareup.kotlinpoet.TypeName {
    return when (kind) {
        TypeKind.BOOLEAN -> BOOLEAN
        TypeKind.BYTE -> BYTE
        TypeKind.SHORT -> SHORT
        TypeKind.INT -> INT
        TypeKind.LONG -> LONG
        TypeKind.CHAR -> CHAR
        TypeKind.FLOAT -> FLOAT
        TypeKind.DOUBLE -> DOUBLE
        TypeKind.ARRAY -> {
            val arrayType = this as ArrayType
            when (arrayType.componentType.kind) {
                TypeKind.BOOLEAN -> BOOLEAN_ARRAY
                TypeKind.BYTE -> BYTE_ARRAY
                TypeKind.SHORT -> SHORT_ARRAY
                TypeKind.INT -> INT_ARRAY
                TypeKind.LONG -> LONG_ARRAY
                TypeKind.CHAR -> CHAR_ARRAY
                TypeKind.FLOAT -> FLOAT_ARRAY
                TypeKind.DOUBLE -> DOUBLE_ARRAY
                else -> if (toString() == "java.lang.String[]") STRING_ARRAY else asTypeName()
            }
        }
        else -> if (toString() == "java.lang.String") STRING else asTypeName()
    }
}

fun TypeElement.packageName(): String {
    var element = this.enclosingElement
    while (element != null && element.kind != ElementKind.PACKAGE) {
        element = element.enclosingElement
    }
    return element?.asType()?.toString() ?: throw IllegalArgumentException("$this does not have an enclosing element of package.")
}

fun TypeMirror.isSubtype(type: TypeMirror): Boolean {
    return AptContext.types.isSubtype(this, type)
}

fun getTypeFromClassName(className: String): TypeMirror {
    return AptContext.elements
        .getTypeElement(className).asType()
}


private val STRING: ClassName = ClassName("kotlin", "String")
private val STRING_ARRAY = ClassName("kotlin", "Array").parameterizedBy(STRING)
private val LONG_ARRAY: ClassName = ClassName("kotlin", "LongArray")
private val INT_ARRAY: ClassName = ClassName("kotlin", "IntArray")
private val SHORT_ARRAY: ClassName = ClassName("kotlin", "ShortArray")
private val BYTE_ARRAY: ClassName = ClassName("kotlin", "ByteArray")
private val CHAR_ARRAY: ClassName = ClassName("kotlin", "CharArray")
private val BOOLEAN_ARRAY: ClassName = ClassName("kotlin", "BooleanArray")
private val FLOAT_ARRAY: ClassName = ClassName("kotlin", "FloatArray")
private val DOUBLE_ARRAY: ClassName = ClassName("kotlin", "DoubleArray")
