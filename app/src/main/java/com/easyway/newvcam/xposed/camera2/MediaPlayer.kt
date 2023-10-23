package com.easyway.newvcam.xposed.camera2

import android.hardware.camera2.CameraDevice
import android.media.MediaPlayer

class MediaPlayer(private val manager: Manager){
    private var mediaPlayer: MediaPlayer? = null;

    fun startFakeVideo(cd: CameraDevice){
        if(mediaPlayer != null)
            return

        val previewSurface = manager.getCameraDevicePreviewSurface(cd) ?: return

        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setSurface(previewSurface)
        mediaPlayer!!.setDataSource("/storage/emulated/0/virtual.mp4")
        mediaPlayer!!.isLooping = true
        mediaPlayer!!.prepare()
        mediaPlayer!!.setOnPreparedListener {
            it.start()
        }
    }
}