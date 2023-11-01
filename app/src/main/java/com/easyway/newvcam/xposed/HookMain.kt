package com.easyway.newvcam.xposed

import android.annotation.SuppressLint
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.easyway.newvcam.xposed.camera2.Manager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.LinkedList
import java.util.TreeMap

interface ICamerasHookable {
    fun containsSurfaceTextureStr(str: String): Boolean
    fun setCameraDevicePreviewSurface(cd: CameraDevice, surface: Surface)
    fun getCameraDevicePreviewSurface(cd: CameraDevice): Surface?
    fun showMessage(message: String)
    var virtualSurface: Surface
}

class HookMain : IXposedHookLoadPackage, ICamerasHookable, XposedExtension() {
    private val TAG = "com.easyway.newvcam"

    override lateinit var virtualSurface: Surface

    private var applicationContext: Context? = null
    private val previewSurfaceTextures = LinkedList<String>()
    private val cameraDevicePreviewSurface = TreeMap<String, Surface>()

    private lateinit var manager: Manager

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        Log.w(TAG, "[MOD NEWVCAM]!")

        lpparam?.let {
            initialize(lpparam)

            initVirtualSurface()

            captureApplicationContext()

            captureTextureSurfaces()

            manager = Manager(this)
        }
    }

    override fun containsSurfaceTextureStr(str: String) = previewSurfaceTextures.contains(str)

    override fun setCameraDevicePreviewSurface(cd: CameraDevice, surface: Surface){
        cameraDevicePreviewSurface[cd.toString()] = surface
    }

    override fun getCameraDevicePreviewSurface(cd: CameraDevice) = cameraDevicePreviewSurface[cd.toString()]

    override fun showMessage(message: String){
        if(applicationContext == null)
            return

        try {
          Toast
              .makeText(applicationContext, message, Toast.LENGTH_SHORT)
              .show()
        } catch (_: NullPointerException){
            Log.e(TAG, "NEWVCAM: $message")
        }
    }

    private fun showInitMessage(){
        showMessage("[NEWVCAM]!")
    }

    @SuppressLint("Recycle")
    private fun initVirtualSurface(){
        virtualSurface = Surface(SurfaceTexture(15))
    }

    private fun captureApplicationContext(){
        hookMethod(
            Instrumentation::class,
            "callApplicationOnCreate",
            { param ->
                when(param.args?.get(0)){
                    is Application -> {
                        applicationContext = (param.args[0] as Application).applicationContext
                        showInitMessage()
                    }
                }
            },
            Application::class
        )
    }

    private fun captureTextureSurfaces(){
        previewSurfaceTextures.push("null")

        hookMethod(
            TextureView::class,
            "getSurfaceTexture"
        ).then {
            previewSurfaceTextures.addFirst(it.toString())
        }
    }
}