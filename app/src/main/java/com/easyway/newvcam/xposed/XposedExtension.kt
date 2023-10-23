package com.easyway.newvcam.xposed

import android.hardware.camera2.CameraDevice
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.NullPointerException
import kotlin.reflect.KClass

interface IXposedExtension {
    fun hookMethod(
        className: String,
        methodName: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit = {},
        vararg parameters: KClass<*> = emptyArray()
    ): XposedExtension.Promisable

    fun <T : Any> hookMethod(
        kClass: KClass<T>,
        methodName: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit = {},
        vararg parameters: KClass<*> = emptyArray()
    ): XposedExtension.Promisable

    fun hookConstructor(
        className: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit = {},
        vararg parameters: KClass<*> = emptyArray()
    ): XposedExtension.Promisable

    fun <T : Any> hookAllConstructors(
        kClass: KClass<T>,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit = {}
    ): XposedExtension.Promisable
}

open class XposedExtension : IXposedExtension {
    fun interface Promisable {
        fun then(cb: (result: Any?) -> Unit)
    }

    private class Callback {
        var hasCallback = false

        var isConstructor = false

        var function: (result: Any?) -> Unit = {}
            set(value) {
                field = value
                hasCallback = true
            }
    }

    private lateinit var lpparam: XC_LoadPackage.LoadPackageParam

    fun initialize(lpparam: XC_LoadPackage.LoadPackageParam){
        this.lpparam = lpparam
    }

    private fun generateHook(
        callback: Callback,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit
    ) : XC_MethodHook {
        return object : XC_MethodHook(){
            override fun beforeHookedMethod(param: MethodHookParam) {
                super.beforeHookedMethod(param)
                hookMethod(param)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)
                if(!callback.hasCallback)
                    return

                var result: Any? = param.thisObject

                if(!callback.isConstructor)
                    try {
                        result = param.result
                    } catch (_: NullPointerException){}

                callback.function(result)
            }
        }
    }

    private fun generate(
        callback: Callback,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit,
        parameters: Array<out KClass<*>>
    ): Array<Any> {
        return arrayOf(
            *parameters.map {
                it.java
            }.toTypedArray(),
            generateHook(callback, hookMethod)
        )
    }

    private fun createPromise(callback: Callback): Promisable {
        return Promisable { cb ->
            with(callback){
                function = cb
            }
        }
    }

    override fun hookMethod(
        className: String,
        methodName: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit,
        vararg parameters: KClass<*>
    ): Promisable {
        val callback = Callback()

        XposedHelpers.findAndHookMethod(
            className,
            lpparam.classLoader,
            methodName,
            *generate(callback, hookMethod, parameters)
        )

        return createPromise(callback)
    }

    override fun <T : Any> hookMethod(
        kClass: KClass<T>,
        methodName: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit,
        vararg parameters: KClass<*>
    ): Promisable {
        val callback = Callback()

        XposedHelpers.findAndHookMethod(
            kClass.java,
            methodName,
            *generate(callback, hookMethod, parameters)
        )

        return createPromise(callback)
    }

    override fun hookConstructor(
        className: String,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit,
        vararg parameters: KClass<*>
    ): Promisable {
        val callback = Callback().apply {
            isConstructor = true
        }

        XposedHelpers.findAndHookConstructor(
            className,
            lpparam.classLoader,
            CameraDevice::class.java,
            *generate(callback, hookMethod, parameters)
        )

        return createPromise(callback)
    }

    override fun <T : Any> hookAllConstructors(
        kClass: KClass<T>,
        hookMethod: (param: XC_MethodHook.MethodHookParam) -> Unit
    ): Promisable {
        val callback = Callback().apply {
            isConstructor = true
        }

        XposedBridge.hookAllConstructors(
            kClass.java,
            generateHook(callback, hookMethod)
        )

        return createPromise(callback)
    }
}