package com.easyway.newvcam.xposed.camera2

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.view.Surface
import com.easyway.newvcam.xposed.HookMain
import com.easyway.newvcam.xposed.ICamerasHookable
import com.easyway.newvcam.xposed.IXposedExtension
import java.util.concurrent.Executor

class Manager(private val hookMain: HookMain) :
    ICamerasHookable by hookMain,
    IXposedExtension by hookMain
{
    private var cameraDevices = mutableListOf<Device>()

    init {
        captureCameraManager()
        captureCaptureRequestBuilder()
    }

    private fun captureStateCallback(stateCallback: StateCallback){
        hookMethod(
            stateCallback::class,
            "onOpened",
            { mhp ->
                val cameraDevice = mhp.args[0] as CameraDevice

                val notHasDevice = cameraDevices.none { it.hasDevice(cameraDevice) }

                if(notHasDevice)
                    cameraDevices.add(Device(cameraDevice, this))
            },
            CameraDevice::class
        )

        hookMethod(
            stateCallback::class,
            "onClosed",
            { mhp ->
                val cameraDevice = mhp.args[0] as CameraDevice

                cameraDevices.removeIf { it.hasDevice(cameraDevice) }
            },
            CameraDevice::class
        )
    }

    private fun first(){
        try {
            hookMethod(
                CameraManager::class,
                "openCamera",
                {
                    showMessage("MANAGER? 1")

                    val callback = it.args[1] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                StateCallback::class,
                Handler::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun second(){
        try {
            hookMethod(
                CameraManager::class,
                "openCamera",
                {
                    showMessage("MANAGER? 2")

                    val callback = it.args[3] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                Boolean::class,
                Handler::class,
                StateCallback::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun tree(){
        try {
            hookMethod(
                CameraManager::class,
                "openCamera",
                {
                    showMessage("MANAGER? 3")

                    val callback = it.args[2] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                Executor::class,
                StateCallback::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun four(){
        try {
            hookMethod(
                CameraManager::class,
                "openCamera",
                {
                    showMessage("MANAGER? 4")

                    val callback = it.args[3] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                Int::class,
                Executor::class,
                StateCallback::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun five(){
        try {
            hookMethod(
                CameraManager::class,
                "openCameraForUid",
                {
                    showMessage("MANAGER? 5")

                    val callback = it.args[1] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                StateCallback::class,
                Executor::class,
                Int::class,
                Int::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun six(){
        try {
            hookMethod(
                CameraManager::class,
                "openCameraForUid",
                {
                    showMessage("MANAGER? 6")

                    val callback = it.args[1] as StateCallback?

                    callback?.let(::captureStateCallback)
                },
                String::class,
                StateCallback::class,
                Executor::class,
                Int::class
            )
        } catch (_: NoSuchMethodError) { }
    }

    private fun captureCameraManager(){
        first()
        second()
        tree()
        four()
        five()
        six()
    }

    private fun captureCaptureRequestBuilder(){
        hookMethod(
            CaptureRequest.Builder::class,
            "addTarget",
            {
                it.args[0] = virtualSurface
            },
            Surface::class
        )
    }
}