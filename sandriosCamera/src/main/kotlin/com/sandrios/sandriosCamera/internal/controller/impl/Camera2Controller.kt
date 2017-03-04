package com.sandrios.sandriosCamera.internal.controller.impl

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration
import com.sandrios.sandriosCamera.internal.configuration.ConfigurationProvider
import com.sandrios.sandriosCamera.internal.controller.CameraController
import com.sandrios.sandriosCamera.internal.controller.view.CameraView
import com.sandrios.sandriosCamera.internal.manager.CameraManager
import com.sandrios.sandriosCamera.internal.manager.impl.Camera2Manager
import com.sandrios.sandriosCamera.internal.manager.listener.CameraCloseListener
import com.sandrios.sandriosCamera.internal.manager.listener.CameraOpenListener
import com.sandrios.sandriosCamera.internal.manager.listener.CameraPhotoListener
import com.sandrios.sandriosCamera.internal.manager.listener.CameraVideoListener
import com.sandrios.sandriosCamera.internal.ui.view.AutoFitTextureView
import com.sandrios.sandriosCamera.internal.utils.CameraHelper
import com.sandrios.sandriosCamera.internal.utils.Size
import java.io.File

/**
 * Created by Arpit Gandhi on 7/6/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Controller(private val cameraView: CameraView, private val configurationProvider: ConfigurationProvider) : CameraController<String>, CameraOpenListener<String, TextureView.SurfaceTextureListener>, CameraPhotoListener, CameraVideoListener, CameraCloseListener<String> {

    override var currentCameraId: String = ""
        private set
    private var camera2Manager: CameraManager<String, TextureView.SurfaceTextureListener>? = null

    override var outputFile: File = null!!
        private set

    override fun onCreate(savedInstanceState: Bundle) {
        camera2Manager = Camera2Manager.instance
        camera2Manager!!.initializeCameraManager(configurationProvider, cameraView.activity)
        currentCameraId = camera2Manager!!.faceBackCameraId
    }

    override fun onResume() {
        camera2Manager!!.openCamera(currentCameraId, this)
    }

    override fun onPause() {
        camera2Manager!!.closeCamera(null)
        cameraView.releaseCameraPreview()
    }

    override fun onDestroy() {
        camera2Manager!!.releaseCameraManager()
    }

    override fun takePhoto() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.activity, CameraConfiguration.MEDIA_ACTION_PHOTO)
        camera2Manager!!.takePhoto(outputFile, this)
    }

    override fun startVideoRecord() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.activity, CameraConfiguration.MEDIA_ACTION_VIDEO)
        camera2Manager!!.startVideoRecord(outputFile, this)
    }

    override fun stopVideoRecord() {
        camera2Manager!!.stopVideoRecord()
    }

    override val isVideoRecording: Boolean
        get() = camera2Manager!!.isVideoRecording

    override fun switchCamera(@CameraConfiguration.CameraFace cameraFace: Int) {
        currentCameraId = if (camera2Manager!!.currentCameraId == camera2Manager!!.faceFrontCameraId)
            camera2Manager!!.faceBackCameraId
        else
            camera2Manager!!.faceFrontCameraId

        camera2Manager!!.closeCamera(this)
    }

    override fun setFlashMode(@CameraConfiguration.FlashMode flashMode: Int) {
        camera2Manager!!.setFlashMode(flashMode)
    }

    override fun switchQuality() {
        camera2Manager!!.closeCamera(this)
    }

    override val numberOfCameras: Int
        get() = camera2Manager!!.numberOfCameras

    override val mediaAction: Int
        get() = configurationProvider.mediaAction

    override fun onCameraOpened(openedCameraId: String, previewSize: Size, surfaceTextureListener: TextureView.SurfaceTextureListener) {
        cameraView.updateUiForMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH)
        cameraView.updateCameraPreview(previewSize, AutoFitTextureView(cameraView.activity, surfaceTextureListener))
        cameraView.updateCameraSwitcher(camera2Manager!!.numberOfCameras)
    }

    override fun onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError")
    }

    override fun onCameraClosed(closedCameraId: String) {
        cameraView.releaseCameraPreview()

        camera2Manager!!.openCamera(currentCameraId, this)
    }

    override fun onPhotoTaken(photoFile: File) {
        cameraView.onPhotoTaken()
    }

    override fun onPhotoTakeError() {}

    override fun onVideoRecordStarted(videoSize: Size) {
        cameraView.onVideoRecordStart(videoSize.width, videoSize.height)
    }

    override fun onVideoRecordStopped(videoFile: File) {
        cameraView.onVideoRecordStop()
    }

    override fun onVideoRecordError() {

    }

    override val cameraManager: CameraManager<*, *>
        get() = camera2Manager

    companion object {

        private val TAG = "Camera2Controller"
    }
}