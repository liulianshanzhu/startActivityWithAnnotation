package com.durian.annotationdemo.utils

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.durian.annotation.Creator
import kotlin.reflect.full.declaredFunctions

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/26 15:44
 */
object InjectUtil {
    private const val CREATOR = "Creator"
    private const val INJECT = "inject"

    private const val CREATOR_KT = "CreatorKt"

    fun bind(activity: Activity, intent: Intent? = null) {
        intentBuilder(activity, intent ?: activity.intent)
    }

//    fun bind(activity: Activity, bundle: Bundle? = null) {
//        intentBuilder(activity, bundle ?: activity.intent.extras)
//    }


    fun bind(frag: Fragment) {
        val clz = frag.javaClass
        if (clz.isAnnotationPresent(Creator::class.java)) {
            try {
                ReflectUtil.getMethod(clz.name + CREATOR,
                    INJECT,
                    clz
                ).invoke(null, frag)
            } catch (e: Exception) {
            }

        }
    }

    fun bind(service: Service, i: Intent) {
        intentBuilder(service, i)
    }

    private fun intentBuilder(o: Any, i: Intent) {
        val clz = o.javaClass
        if (clz.isAnnotationPresent(Creator::class.java)) {
            try {
                ReflectUtil.getMethod(clz.name + CREATOR,
                    INJECT,
                    clz,
                    Intent::class.java
                ).invoke(null, o, i)
            } catch (e: Exception) {
                try {
                    ReflectUtil.getMethod(clz.name + CREATOR_KT,
                        INJECT,
                        clz,
                        Intent::class.java
                    ).invoke(null, o, i)
                }catch (e: Exception){

                }
            }
        }
    }

    private fun intentBuilder(o: Any, i: Bundle?) {
        try {
            Class.forName(o.javaClass.getName() + CREATOR)
                .getDeclaredMethod("inject", Activity::class.java, Intent::class.java)
                .invoke(null, o, i)
        } catch (e: Exception) {
            Log.e("HZMDurian",  "intentBuilder error = ${e}  ${o.javaClass.getName() + CREATOR}")
        }

    }


}
