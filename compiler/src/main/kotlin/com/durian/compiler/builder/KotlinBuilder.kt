package com.durian.compiler.builder

import com.durian.compiler.android.Java
import com.durian.compiler.android.Kotlin
import com.durian.compiler.base.ClassInfo
import com.durian.compiler.base.Field
import com.durian.compiler.utils.asKotlinTypeName
import com.squareup.kotlinpoet.*
import java.io.Serializable
import java.util.*

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31
 */
class KotlinBuilder {

    fun build(classInfo: ClassInfo): FileSpec {
        val annotatedTypeName = classInfo.typeElement.asType().asKotlinTypeName()
        val name = String.format("%sCreatorKt", classInfo.simpleName)

        /**
         * 划重点，巨坑，需要添加注解(JvmName)，需要添加注解,需要添加注解，
         * 否则反射调用inject会出现ClassNotFoundException，且注解名不能跟.java的名字冲突
         */
        val fileBuilder = FileSpec.builder(classInfo.packageName, name)
            .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember("\"$name\"").build())


        if (classInfo.isActivity) {
            //activity
            //创建start方法
            val startMethod = FunSpec.builder("start${classInfo.simpleName}")
                .receiver(Kotlin.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .addStatement("val intent = %T(this, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .returns(UNIT)
            addKotlinParams(startMethod, classInfo.required, classInfo.optional, classInfo.fields)
            startMethod.addStatement("startActivity(intent)")
            fileBuilder.addFunction(startMethod.build())
            //创建start方法带动画
            val startWithAnimMethod = FunSpec.builder("start${classInfo.simpleName}")
                .receiver(Kotlin.ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .addStatement("val intent = %T(this, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .returns(UNIT)
            addKotlinParams(startWithAnimMethod, classInfo.required, classInfo.optional, classInfo.fields)
            startWithAnimMethod.addParameter(ParameterSpec.builder("enter", INT).defaultValue("0").build())
                .addParameter(ParameterSpec.builder("exit", INT).defaultValue("0").build())
            startWithAnimMethod.addStatement("startActivity(intent)")
                .addStatement("overridePendingTransition(enter, exit)")
            fileBuilder.addFunction(startWithAnimMethod.build())

            // 创建Activity.start创建ActivityForResult方法
            val startForResultMethod = FunSpec.builder("start${classInfo.simpleName}ForResult")
                .receiver(Kotlin.ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .addParameter("code", INT)
                .addStatement("val intent = %T(this, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .returns(UNIT)
            addKotlinParams(startForResultMethod,classInfo.required, classInfo.optional, classInfo.fields)
            startForResultMethod.addStatement("startActivityForResult(intent, code)")
            fileBuilder.addFunction(startForResultMethod.build())
            //创建Fragment.start创建ActivityForResult方法=
            val startFragmentForResultMethod = FunSpec.builder("start${classInfo.simpleName}ForResult")
                .receiver(Kotlin.FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .addParameter("code", INT)
                .addStatement("val intent = %T(this.context, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .returns(UNIT)
            addKotlinParams(startFragmentForResultMethod, classInfo.required, classInfo.optional, classInfo.fields)
            startFragmentForResultMethod.addStatement("startActivityForResult(intent, code)")
            fileBuilder.addFunction(startFragmentForResultMethod.build())

            creatSaveState(classInfo, fileBuilder)
        } else if (classInfo.isService) {
            //服务，
            //生成startService方法
            val startMethod = FunSpec.builder("start${classInfo.simpleName}")
                .receiver(Kotlin.CONTEXT)
                .addStatement("val intent = %T(this, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .addModifiers(KModifier.PUBLIC)
                .returns(UNIT)
            addKotlinParams(startMethod, classInfo.required, classInfo.optional, classInfo.fields)
            startMethod
            startMethod.beginControlFlow("if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)")
                .addStatement("startForegroundService(intent)")
                .nextControlFlow("else")
                .addStatement("startService(intent)")
                .endControlFlow()
            fileBuilder.addFunction(startMethod.build())
            // 生成stop方法
            val stopMethod = FunSpec.builder("stop${classInfo.simpleName}")
                .receiver(Kotlin.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(UNIT)
                .addStatement("val intent = %T(this, %T::class.java)", Kotlin.INTENT, classInfo.typeElement)
                .addStatement("stopService(intent)")
            fileBuilder.addFunction(stopMethod.build())
        } else if (classInfo.isFragment) {
            // fragment
            //生成get()方法
            val returnType = classInfo.typeElement.asType().asKotlinTypeName()
            val getMethod = FunSpec.builder("get${classInfo.simpleName}")
                .receiver(Kotlin.CONTEXT)
                .addStatement("val bundle = %T()", Kotlin.BUNDLE)
                .addStatement("val frag = %T()", returnType)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
            addKotlinParams(getMethod, classInfo.required, classInfo.optional, classInfo.fields, false)
            addBundleStatement(getMethod, classInfo.fields)
            getMethod
                .addStatement("frag.arguments = bundle")
                .addStatement("return frag")
            fileBuilder.addFunction(getMethod.build())

            creatSaveState(classInfo, fileBuilder)
        }

        /**
         * 生成inject方法
         */
        val returnType = classInfo.typeElement.asType().asKotlinTypeName()
        val injectMethod = FunSpec.builder("injectBundle")
            .addModifiers(KModifier.PUBLIC)
            .addParameter("host", returnType)
            .addParameter("bundle", Kotlin.BUNDLE)
        injectMethod.beginControlFlow("if (bundle == null)")
            .addStatement("return")
            .endControlFlow()
        classInfo.fields.forEach {
            val paramName = it.name
            val typeName = it.asKotlinTypeName().asNonNullable()
            val typeNameNullable = it.asKotlinTypeName().asNullable()
            if (it.isPrimitive) {
                injectMethod.beginControlFlow("if (bundle.containsKey(%S))", paramName)
                    .addStatement("val %N = bundle.get%T(%S)", paramName, typeName, paramName)
                    .addStatement("host.%N = %N",paramName, paramName )
                    .endControlFlow()
            } else {
                if (typeName == Kotlin.STRING) {
                    injectMethod.beginControlFlow("if (bundle.containsKey(%S))", paramName)
                        .addStatement("val %N: %T = bundle.get%T(%S)", paramName, typeNameNullable, typeName, paramName)
                        .beginControlFlow("if (%N != null)", paramName)
                        .addStatement("host.%N = %N",paramName, paramName )
                        .endControlFlow()
                        .endControlFlow()
                } else {
                    injectMethod.beginControlFlow("if (bundle.containsKey(%S))", paramName)
                        .addStatement("val %N = bundle.get%T(%S)", paramName,  Serializable::class.java, paramName)
                        .beginControlFlow("if (%N != null)", paramName)
                        .addStatement("host.%N = %N as %T",paramName, paramName, typeName)
                        .endControlFlow()
                        .endControlFlow()
                }
            }
        }

        fileBuilder.addFunction(injectMethod.build())
        creatInjectState(classInfo, annotatedTypeName, fileBuilder)

        return fileBuilder.build()
    }

    /**
     * 数据持久化读取
     */
    private fun creatInjectState(classInfo: ClassInfo, annotatedTypeName:TypeName, typeBuilder: FileSpec.Builder) {
        if (classInfo.isActivity || classInfo.isFragment) {
            val returnType = classInfo.typeElement.asType().asKotlinTypeName()
            val injectMethod = FunSpec.builder("inject")
                .addModifiers(KModifier.PUBLIC)
            if (classInfo.isFragment) {
                injectMethod.addParameter("host", returnType)
                    .addStatement("val extras = host.arguments")
            }else {
                injectMethod.addParameter("host", returnType)
                    .addParameter("intent", Kotlin.INTENT)
                    .addStatement("%T.e(\"HZMDurian\",\"初始化\")", Kotlin.LOG)
                    .addStatement("val extras = intent.extras")
            }
            injectMethod.beginControlFlow("if (extras == null)")
                .addStatement("return")
                .endControlFlow()
            classInfo.fields.forEach {
                val paramName = it.name
                val typeName = it.asKotlinTypeName().asNonNullable()
                val typeNameNullable = it.asKotlinTypeName().asNullable()
                if (it.isPrimitive) {
                    injectMethod.beginControlFlow("if (extras.containsKey(%S))", paramName)
                        .addStatement("val %N = extras.get%T(%S)", paramName, typeName, paramName)
                        .addStatement("host.%N = %N",paramName, paramName )
                        .endControlFlow()
                } else {
                    if (typeName == Kotlin.STRING) {
                        injectMethod.beginControlFlow("if (extras.containsKey(%S))", paramName)
                            .addStatement("val %N: %T = extras.get%T(%S)", paramName, typeNameNullable, typeName, paramName)
                            .beginControlFlow("if (%N != null)", paramName)
                            .addStatement("host.%N = %N",paramName, paramName )
                            .endControlFlow()
                            .endControlFlow()
                    } else {
                        injectMethod.beginControlFlow("if (extras.containsKey(%S))", paramName)
                            .addStatement("val %N = extras.get%T(%S)", paramName,  Serializable::class.java, paramName)
                            .beginControlFlow("if (%N != null)", paramName)
                            .addStatement("host.%N = %N as %T",paramName, paramName, typeName)
                            .endControlFlow()
                            .endControlFlow()
                    }
                }
            }

            typeBuilder.addFunction(injectMethod.build())
        }
    }

    /**
     * 数据持久化存储
     */
    private fun creatSaveState(classInfo: ClassInfo, typeBuilder: FileSpec.Builder) {
        val methodBuilder = FunSpec.builder("saveState")
            .addModifiers(KModifier.PUBLIC)
            .returns(UNIT)
            .addParameter("instance", classInfo.typeElement.asType().asKotlinTypeName())
            .addParameter("outState", Kotlin.BUNDLE)

        methodBuilder.addStatement("val intent = %T()", Kotlin.INTENT)
        classInfo.fields.forEach {
            val paramName = it.name
            methodBuilder.addStatement("intent.putExtra(%S, instance.%N)", paramName, paramName)
        }
        methodBuilder.addStatement("outState.putAll(intent.extras)")

        typeBuilder.addFunction(methodBuilder.build())
    }

    private fun addKotlinParams(
        method: FunSpec.Builder,
        required: List<Field>,
        optional: List<Field>,
        all: TreeSet<Field>,
        needAddIntent:Boolean = true) {
        required.forEach {
            val paramName = it.name
            val typeName = it.asKotlinTypeName()
            method.addParameter(paramName, typeName)
        }
        optional.forEach {
            val paramName = it.name
            val typeName = it.asKotlinTypeName().asNullable() //可空类型
            method.addParameter(ParameterSpec.builder(paramName, typeName).defaultValue("null").build())
        }
        if (needAddIntent) {
            all.forEach {
                val paramName = it.name
                method.addStatement("intent.putExtra(%S, %N)", paramName, paramName)
            }
        }
    }

    private fun addBundleStatement(builder: FunSpec.Builder, fields: TreeSet<Field>) {
        fields.forEach {
            val paramName = it.name
            val typeName = it.asKotlinTypeName()
            if (it.isPrimitive) {
                builder.addStatement("bundle.put%T(%S, %N)", typeName, paramName, paramName)
            } else {
                if (typeName == Kotlin.STRING) {
                    builder.addStatement("bundle.put%T(%S, %N)", typeName, paramName, paramName)
                } else {
                    builder.addStatement("bundle.put%T(%S, %N)", Serializable::class.java, paramName, paramName)
                }
            }
        }
    }
}