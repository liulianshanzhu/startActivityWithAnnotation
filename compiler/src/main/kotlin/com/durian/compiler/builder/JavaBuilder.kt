package com.durian.compiler.builder

import com.durian.compiler.android.Java
import com.durian.compiler.base.ClassInfo
import com.durian.compiler.base.Field
import com.durian.compiler.utils.getTypeName
import com.squareup.javapoet.*
import java.io.Serializable
import java.util.*
import javax.lang.model.element.Modifier

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31
 */
class JavaBuilder {

    fun build(classInfo: ClassInfo): TypeSpec {
        val annotatedTypeName = classInfo.typeElement.getTypeName()
        val fileName = String.format("%sCreator", classInfo.simpleName)
        val builder = TypeSpec.classBuilder(fileName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        /**
         * 生成create方法
         */
        val clzName = ClassName.get(classInfo.packageName, fileName)
        val createMethod = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("$fileName builder = new $fileName()")
            .returns(clzName)
        // 添加必须的参数
        classInfo.required.forEach {
            val paramName = it.name
            builder.addField(it.asJavaTypeName(), paramName, Modifier.PRIVATE)

            createMethod.addParameter(createNullableParam(it.asJavaTypeName(), paramName))
            createMethod.addStatement("builder.\$N = \$N", paramName, paramName)
        }
        createMethod.addStatement("return builder")
        builder.addMethod(createMethod.build())
        /**
         * 根据optional生成链式调用的方法
         */
        classInfo.optional.forEach {
            val paramName = it.name
            builder.addField(it.asJavaTypeName(), paramName, Modifier.PRIVATE)

            builder.addMethod(
                MethodSpec.methodBuilder(paramName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(it.asJavaTypeName(), paramName)
                    .addStatement("this.\$N = \$N", paramName, paramName)
                    .addStatement("return this")
                    .returns(ClassName.get(classInfo.packageName, fileName))
                    .build()
            )
        }
        /**
         * 生成new intent方法
         */
        if (classInfo.isActivity || classInfo.isService) {
            // 只有activity和service需要
            val newIntentMethod = MethodSpec.methodBuilder("newIntent")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(createNonNullParam(Java.CONTEXT, "context"))
                .addStatement("\$T intent = new Intent(context, \$T.class)", Java.INTENT, annotatedTypeName)
                .returns(Java.INTENT)
            classInfo.fields.forEach {
                newIntentMethod.addParameter(createNullableParam(it.asJavaTypeName(), it.name))
            }

            addIntentStatement(newIntentMethod, classInfo.fields)
            newIntentMethod.addStatement("return intent")
            builder.addMethod(newIntentMethod.build())
        }
        if (classInfo.isService) {
            // 服务
            /*
             * 生成 start 方法，服务开启方式需要兼容Andorid8.0，由于NotificationManager具有不确定性，因此不自动生成
             */
            val startMethod = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(createNullableParam(Java.CONTEXT, "context"))
                .addStatement("if(context == null) return")
                .addStatement("\$T intent = new Intent(context, \$T.class)", Java.INTENT, annotatedTypeName)
            addIntentStatement(startMethod, classInfo.fields)
            startMethod.beginControlFlow("if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)")
                .addStatement("context.startForegroundService(intent)")
                .nextControlFlow("else")
                .addStatement("context.startService(intent)")
                .endControlFlow()
            builder.addMethod(startMethod.build())

            /*
             *生成 stop 方法
             */
            val stopMethod = MethodSpec.methodBuilder("stop")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(createNullableParam(Java.CONTEXT, "context"))
                .addStatement("if(context == null) return")
                .addStatement("\$T intent = new Intent(context, \$T.class)", Java.INTENT, annotatedTypeName)
                .addStatement("context.stopService(intent)")
            builder.addMethod(stopMethod.build())

        } else if (classInfo.isFragment) {
            // fragment
            val buildMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("\$T b = new \$T()", Java.BUNDLE, Java.BUNDLE)
            addBundleStatement(buildMethod, classInfo.fields)
            buildMethod.addStatement("\$T frag = new \$T()", annotatedTypeName, annotatedTypeName)
                .addStatement("frag.setArguments(b)")
                .addStatement("return frag")
                .returns(annotatedTypeName)
            builder.addMethod(buildMethod.build())

            creatSaveState(classInfo, builder)
        } else if (classInfo.isActivity) {
            // activity
            /*
             * 生成 start 方法
             */
            val startMethod = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(createNullableParam(Java.CONTEXT, "context"))
                .addStatement("if(context == null) return")
                .addStatement(
                    "\$T intent = new \$T(context, \$T.class)",
                    Java.INTENT,
                    Java.INTENT,
                    annotatedTypeName
                )
            addIntentStatement(startMethod, classInfo.fields)
            startMethod.beginControlFlow("if (!(context instanceof \$T))", Java.ACTIVITY)
                .addStatement("intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)")
                .endControlFlow()
                .addStatement("context.startActivity(intent)")
            builder.addMethod(startMethod.build())

            /*
             * 生成 start 方法，带动画
             */
            val startWithAnimMethod = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(createNullableParam(Java.CONTEXT, "context"))
                .addParameter(TypeName.INT, "enter")
                .addParameter(TypeName.INT, "exit")
                .addStatement("if(context == null) return")
                .addStatement(
                    "\$T intent = new \$T(context, \$T.class)",
                    Java.INTENT,
                    Java.INTENT,
                    annotatedTypeName
                )
            addIntentStatement(startWithAnimMethod, classInfo.fields)
            startWithAnimMethod.beginControlFlow("if (!(context instanceof \$T))", Java.ACTIVITY)
                .addStatement("intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)")
                .endControlFlow()
                .addStatement("context.startActivity(intent)")
                .beginControlFlow("if (context instanceof \$T)", Java.ACTIVITY)
                .addStatement("((Activity) context).overridePendingTransition(enter, exit)")
                .endControlFlow()
            builder.addMethod(startWithAnimMethod.build())

            /*
             * 生成 start for result 方法
             */
            val hostName = "objectHost"
            val startForResultMethod = MethodSpec.methodBuilder("start")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Any::class.java, hostName)
                .addParameter(TypeName.INT, "code")
                .addStatement("Context context = null")
                .beginControlFlow("if (\$N instanceof \$T)", hostName, Java.ACTIVITY)
                .addStatement("context = (Context) \$N", hostName)
                .nextControlFlow("else if (\$N instanceof \$T)", hostName, Java.FRAGMENT)
                .addStatement(" context = ((Fragment) \$N).getContext()", hostName)
                .nextControlFlow("else")
                .addStatement(
                    "throw new \$T(\"\$N must be one of activity or fragment\")",
                    IllegalStateException::class.java,
                    hostName
                )
                .endControlFlow()
                .addStatement("\$T intent = new Intent(context, \$T.class)", Java.INTENT, annotatedTypeName)

            addIntentStatement(startForResultMethod, classInfo.fields)

            startForResultMethod.beginControlFlow("if (\$N instanceof \$T)", hostName, Java.ACTIVITY)
                .addStatement("((\$T) \$N).startActivityForResult(intent, code)", Java.ACTIVITY, hostName)
                .nextControlFlow("else if (\$N instanceof \$T)", hostName, Java.FRAGMENT)
                .addStatement("((\$T) \$N).startActivityForResult(intent, code)", Java.FRAGMENT, hostName)
                .endControlFlow()

            builder.addMethod(startForResultMethod.build())

            creatSaveState(classInfo, builder)
        }
        /*
         * 生成objectHost调用的inject方法
         */
        var injectMethod: MethodSpec.Builder? = null
        if (classInfo.isFragment) {
            injectMethod = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(annotatedTypeName, "objectHost")
                .addStatement("\$T extras = objectHost.getArguments()", Java.BUNDLE)
        } else {
            injectMethod = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(annotatedTypeName, "objectHost")
                .addParameter(Java.INTENT, "intent")
                .addStatement("\$T extras = intent.getExtras()", Java.BUNDLE)
        }
        injectMethod.beginControlFlow("if (extras == null)")
            .addStatement("return")
            .endControlFlow()
        classInfo.fields.forEach {
            var paramName = it.name
            var cs: MutableList<Char>? = null
            paramName.toCharArray().forEach {
                cs?.add(it)
            }

            injectMethod.beginControlFlow("if (extras.containsKey(\$S))", paramName)
            // 判断是否boolean而且带有is声明, 需要去掉is
            val tn = it.asJavaTypeName()
            if (tn == Java.BOOLEAN && paramName.startsWith("is")) {
                val realName = paramName.substring(2)
                injectMethod.addStatement(
                    "objectHost.set\$N((\$T) extras.get(\$S))",
                    realName,
                    it.asJavaTypeName(),
                    paramName
                )
            } else {
                val realName =
                    StringBuilder().append(Character.toUpperCase(paramName[0])).append(paramName.substring(1))
                        .toString()
                injectMethod.addStatement(
                    "objectHost.set\$N((\$T) extras.get(\$S))",
                    realName,
                    it.asJavaTypeName(),
                    paramName
                )
            }
            injectMethod.endControlFlow()
        }
        builder.addMethod(injectMethod.build())
        creatInjectState(classInfo, annotatedTypeName, builder)
        return builder.build()
    }

    /**
     * 数据持久化读取
     */
    private fun creatInjectState(classInfo: ClassInfo, annotatedTypeName:TypeName, builder: TypeSpec.Builder){
        if (classInfo.isActivity || classInfo.isFragment) {
            var injectMethod: MethodSpec.Builder? = null
            injectMethod = MethodSpec.methodBuilder("injectBundle")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(annotatedTypeName, "objectHost")
                .addParameter(Java.BUNDLE, "bundle")
            injectMethod.beginControlFlow("if (bundle == null)")
                .addStatement("return")
                .endControlFlow()
            classInfo.fields.forEach {
                var paramName = it.name
                var cs: MutableList<Char>? = null
                paramName.toCharArray().forEach {
                    cs?.add(it)
                }

                injectMethod.beginControlFlow("if (bundle.containsKey(\$S))", paramName)
                // 判断是否boolean而且带有is声明, 需要去掉is
                val tn = it.asJavaTypeName()
                if (tn == Java.BOOLEAN && paramName.startsWith("is")) {
                    val realName = paramName.substring(2)
                    injectMethod.addStatement(
                        "objectHost.set\$N((\$T) bundle.get(\$S))",
                        realName,
                        it.asJavaTypeName(),
                        paramName
                    )
                } else {
                    val realName =
                        StringBuilder().append(Character.toUpperCase(paramName[0])).append(paramName.substring(1))
                            .toString()
                    injectMethod.addStatement(
                        "objectHost.set\$N((\$T) bundle.get(\$S))",
                        realName,
                        it.asJavaTypeName(),
                        paramName
                    )
                }
                injectMethod.endControlFlow()
            }
            builder.addMethod(injectMethod.build())
        }
    }

    /**
     * 数据持久化存储
     */
    private fun creatSaveState(classInfo: ClassInfo, typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("saveState")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.VOID)
            .addParameter(if (classInfo.isActivity) Java.ACTIVITY else Java.FRAGMENT, "instance")
            .addParameter(Java.BUNDLE, "outState")
            .beginControlFlow("if(instance instanceof \$T)", classInfo.typeElement)
            .addStatement("\$T typedInstance = (\$T) instance", classInfo.typeElement, classInfo.typeElement)
        methodBuilder.addStatement("\$T intent = new \$T()", Java.INTENT, Java.INTENT)

        for (field in classInfo.fields) {
            val name = field.name
            if (field.isPrivate) {
                methodBuilder.addStatement("intent.putExtra(\$S, typedInstance.get\$L())", name, name.capitalize())
            } else {
                methodBuilder.addStatement("intent.putExtra(\$S, typedInstance.\$L)", name, name)
            }
        }

        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow()

        typeBuilder.addMethod(methodBuilder.build())
    }

    private fun createNullableParam(typeName: TypeName, name: String): ParameterSpec {
        val builder = ParameterSpec.builder(typeName, name)
        builder.addAnnotation(Java.NULLABLE)
        return builder.build()
    }

    private fun createNonNullParam(className: ClassName, name: String): ParameterSpec {
        val builder = ParameterSpec.builder(className, name)
        builder.addAnnotation(Java.NON_NULL)
        return builder.build()
    }

    private fun addIntentStatement(builder: MethodSpec.Builder, fields: TreeSet<Field>) {
        fields.forEach {
            val paramName = it.name
            builder.beginControlFlow("if (\$N != null)", paramName)
                .addStatement("intent.putExtra(\$S, \$N)", paramName, paramName)
                .endControlFlow()
        }
    }

    private fun addBundleStatement(builder: MethodSpec.Builder, fields: TreeSet<Field>) {
        fields.forEach {
            val paramName = it.name
            builder.beginControlFlow("if (\$N != null)", paramName)
            val typeName = it.asJavaTypeName()
            if (it.isPrimitive) {
                // Long Boolean Integer...
                if (typeName.unbox() === TypeName.INT) {
                    builder.addStatement("b.put\$N(\$S, \$N)", "Int", paramName, paramName)
                } else {
                    builder.addStatement("b.put\$T(\$S, \$N)", typeName, paramName, paramName)
                }
            } else {
                // 判断是否为String, serialize等, FIXME: 可以自行添加类型
                if (typeName == Java.STRING) {
                    builder.addStatement("b.put\$T(\$S, \$N)", typeName, paramName, paramName)
                } else {
                    builder.addStatement("b.put\$T(\$S, \$N)", Serializable::class.java, paramName, paramName)
                }
            }

            builder.endControlFlow()
        }
    }

}