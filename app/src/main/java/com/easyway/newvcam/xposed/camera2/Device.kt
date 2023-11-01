package com.easyway.newvcam.xposed.camera2

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.InputConfiguration
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Handler
import android.view.Surface
import com.easyway.newvcam.xposed.IXposedExtension

class Device (
    private val cd: CameraDevice,
    private val manager: Manager
) : IXposedExtension by manager {
    private var mediaPlayer: MediaPlayer? = null

    init {
        one()
        two()
        tree()
        four()
        five()
        six()
        seven()
    }

    fun hasDevice(cameraDevice: CameraDevice) = cd == cameraDevice

    private fun isPreviewSurface(surface: Surface): Boolean {
        val surfaceTexture = Regex("Surface\\(name=(.*)\\)")
            .find(surface.toString())
            ?.let {
                it.groupValues[1]
            }

        return surfaceTexture?.let { manager.containsSurfaceTextureStr(it) } ?: false
    }

    private fun getPreviewSurface(surfaces: List<*>) =
        surfaces
            .filterIsInstance<Surface>()
            .first(::isPreviewSurface)

    private fun getPreviewSurface2(surfaces: List<*>): Surface {
        surfaces.forEach(){
            val surface = it as Surface

            manager.showMessage("SURFACE ANALYZED: $surface")
        }

        return surfaces
            .filterIsInstance<Surface>()
            .first(::isPreviewSurface)
    }

    @Suppress("DuplicatedCode")
    private fun one(){
        hookMethod(
            cd::class,
            "createCaptureSession",
            { mhp ->
                manager.showMessage("DEVICE? 1")

                try {
                    val previewSurface = getPreviewSurface(mhp.args[0] as List<*>)

                    manager.setCameraDevicePreviewSurface(cd, previewSurface)
                } catch (_: NoSuchElementException){}

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }

                mhp.args[0] = listOf(manager.virtualSurface)
            },
            List::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }

    private fun processOutputConfiguration(outputConfiguration: OutputConfiguration): OutputConfiguration {
        var previewSurface: Surface? = null

        try {
            previewSurface = getPreviewSurface(outputConfiguration.surfaces)
        } catch (_: NoSuchElementException){}

        if(previewSurface != null)
            manager.setCameraDevicePreviewSurface(cd, previewSurface)

        return OutputConfiguration(manager.virtualSurface)
    }

    private fun two(){
        hookMethod(
            cd::class,
            "createCaptureSession",
            { mhp ->
                manager.showMessage("DEVICE? 2")

                val sessionConfiguration = mhp.args[0] as SessionConfiguration

                mhp.args[0] = SessionConfiguration(
                    sessionConfiguration.sessionType,
                    sessionConfiguration.outputConfigurations.map(::processOutputConfiguration),
                    sessionConfiguration.executor,
                    sessionConfiguration.stateCallback
                )

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }
            },
            SessionConfiguration::class
        )
    }

    private fun tree(){
        hookMethod(
            cd::class,
            "createCaptureSessionByOutputConfigurations",
            { mhp ->
                manager.showMessage("DEVICE? 3")

                val outputConfigurations = (mhp.args[0] as List<*>)
                    .filterIsInstance<OutputConfiguration>()

                mhp.args[0] = outputConfigurations.map(::processOutputConfiguration)

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }
            },
            List::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }

    private fun four(){
        hookMethod(
            cd::class,
            "createReprocessableCaptureSession",
            { mhp ->
                manager.showMessage("DEVICE? 4")

                try {
                    val previewSurface = getPreviewSurface(mhp.args[1] as List<*>)

                    manager.setCameraDevicePreviewSurface(cd, previewSurface)
                } catch (_: NoSuchElementException){}

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }

                mhp.args[1] = listOf(manager.virtualSurface)
            },
            InputConfiguration::class,
            List::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }

    private fun five(){
        hookMethod(
            cd::class,
            "createReprocessableCaptureSessionByConfigurations",
            { mhp ->
                manager.showMessage("DEVICE? 5")

                val outputConfigurations = (mhp.args[1] as List<*>)
                    .filterIsInstance<OutputConfiguration>()

                mhp.args[1] = outputConfigurations.map(::processOutputConfiguration)

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }
            },
            InputConfiguration::class,
            List::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }

    @Suppress("DuplicatedCode")
    private fun six(){
        hookMethod(
            cd::class,
            "createConstrainedHighSpeedCaptureSession",
            { mhp ->
                manager.showMessage("DEVICE? 6")

                try {
                    val previewSurface = getPreviewSurface(mhp.args[0] as List<*>)

                    manager.setCameraDevicePreviewSurface(cd, previewSurface)
                } catch (_: NoSuchElementException) { }

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }

                mhp.args[0] = listOf(manager.virtualSurface)
            },
            List::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }

    private fun seven(){
        hookMethod(
            cd::class,
            "createCustomCaptureSession",
            { mhp ->
                manager.showMessage("DEVICE? 7")

                val outputConfigurations = (mhp.args[1] as List<*>)
                    .filterIsInstance<OutputConfiguration>()

                mhp.args[1] = outputConfigurations.map(::processOutputConfiguration)

                mediaPlayer = MediaPlayer(manager)
                    .apply {
                        startFakeVideo(cd)
                    }
            },
            InputConfiguration::class,
            List::class,
            Int::class,
            CameraCaptureSession.StateCallback::class,
            Handler::class
        )
    }
}