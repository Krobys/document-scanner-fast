/**
    Copyright 2021 Krobys

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
    associated documentation files (the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute,
    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or
    substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.krobys.documentscanner.ui.camerascreen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tbruyelle.rxpermissions3.RxPermissions
import com.krobys.documentscanner.R
import com.krobys.documentscanner.common.extensions.hide
import com.krobys.documentscanner.common.extensions.show
import com.krobys.documentscanner.common.utils.FileUriUtils
import com.krobys.documentscanner.manager.SessionManager
import com.krobys.documentscanner.model.DocumentScannerErrorModel
import com.krobys.documentscanner.ui.base.BaseFragment
import com.krobys.documentscanner.ui.components.scansurface.ScanSurfaceListener
import com.krobys.documentscanner.ui.scan.InternalScanActivity
import id.zelory.compressor.determineImageRotation
import kotlinx.android.synthetic.main.fragment_camera_screen.*
import java.io.File
import java.io.FileNotFoundException


internal class CameraScreenFragment: BaseFragment(), ScanSurfaceListener  {

    companion object {
        private const val GALLERY_REQUEST_CODE = 878
        private val TAG = CameraScreenFragment::class.simpleName

        fun newInstance(): CameraScreenFragment {
            return CameraScreenFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanSurfaceView.lifecycleOwner = this
        scanSurfaceView.listener = this
        scanSurfaceView.originalImageFile = getScanActivity().originalImageFile

        checkForCameraPermissions()
        initListeners()

        // settings
        val sessionManager = SessionManager(getScanActivity())
        galleryButton.visibility = if (sessionManager.isGalleryEnabled()) View.VISIBLE else View.GONE
        autoButton.visibility = if (sessionManager.isCaptureModeButtonEnabled()) View.VISIBLE else View.GONE
        scanSurfaceView.isAutoCaptureOn = sessionManager.isAutoCaptureEnabledByDefault()
        scanSurfaceView.isLiveDetectionOn = sessionManager.isLiveDetectionEnabled()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(getScanActivity().shouldCallOnClose) {
            getScanActivity().onClose()
        }
    }

    override fun onResume() {
        super.onResume()
        getScanActivity().reInitOriginalImageFile()
        scanSurfaceView.originalImageFile = getScanActivity().originalImageFile
    }

    private fun initListeners() {
        cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        cancelButton.setOnClickListener {
            finishActivity()
        }
        flashButton.setOnClickListener {
            switchFlashState()
        }
        galleryButton.setOnClickListener {
            checkForStoragePermissions()
        }
        autoButton.setOnClickListener {
            toggleAutoManualButton()
        }
    }

    private fun toggleAutoManualButton() {
        scanSurfaceView.isAutoCaptureOn = !scanSurfaceView.isAutoCaptureOn
        scanSurfaceView.isLiveDetectionOn = scanSurfaceView.isAutoCaptureOn
        if (scanSurfaceView.isAutoCaptureOn) {
            autoButton.text = getString(R.string.zdc_auto)
        } else {
            autoButton.text = getString(R.string.zdc_manual)
        }
    }

    private fun checkForCameraPermissions() {
        RxPermissions(this)
            .requestEach(Manifest.permission.CAMERA)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        startCamera()
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.CAMERA_PERMISSION_REFUSED_WITHOUT_NEVER_ASK_AGAIN))
                    }
                    else -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.CAMERA_PERMISSION_REFUSED_GO_TO_SETTINGS))
                    }
                }
            }
    }

    private fun checkForStoragePermissions() {
        RxPermissions(this)
            .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        selectImageFromGallery()
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.STORAGE_PERMISSION_REFUSED_WITHOUT_NEVER_ASK_AGAIN))
                    }
                    else -> {
                        onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.STORAGE_PERMISSION_REFUSED_GO_TO_SETTINGS))
                    }
                }
            }
    }

    private fun startCamera() {
        scanSurfaceView.start()
    }

    private fun takePhoto() {
        scanSurfaceView.takePicture()
    }

    private fun getScanActivity(): InternalScanActivity {
        return (requireActivity() as InternalScanActivity)
    }

    private fun finishActivity() {
        getScanActivity().finish()
    }

    private fun switchFlashState() {
        scanSurfaceView.switchFlashState()
    }

    override fun showFlash() {
        flashButton?.show()
    }

    override fun hideFlash() {
        flashButton?.hide()
    }

    private fun selectImageFromGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
            try {
                val imageUri = data?.data
                if (imageUri != null) {
                    val realPath = FileUriUtils.getRealPath(getScanActivity(), imageUri)
                    if (realPath != null) {
                        getScanActivity().reInitOriginalImageFile()
                        getScanActivity().originalImageFile = File(realPath)
                        gotoNext()
                    } else {
                        Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR.error)
                        onError(DocumentScannerErrorModel(
                            DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, null))
                    }
                } else {
                    Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR.error)
                    onError(DocumentScannerErrorModel(
                        DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, null))
                }
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "FileNotFoundException", e)
                onError(DocumentScannerErrorModel(
                    DocumentScannerErrorModel.ErrorMessage.TAKE_IMAGE_FROM_GALLERY_ERROR, e))
            }
        }
    }

    override fun scanSurfacePictureTaken() {
        gotoNext()
    }

    private fun gotoNext() {
        if (isAdded) {
            if (SessionManager(getScanActivity()).isCropperEnabled()) {
                getScanActivity().showImageCropFragment()
            } else {
                val sourceBitmap = BitmapFactory.decodeFile(getScanActivity().originalImageFile.absolutePath)
                if (sourceBitmap != null) {
                    getScanActivity().croppedImage = determineImageRotation(getScanActivity().originalImageFile, sourceBitmap)
                    getScanActivity().showImageProcessingFragment()
                } else {
                    Log.e(TAG, DocumentScannerErrorModel.ErrorMessage.INVALID_IMAGE.error)
                    onError(DocumentScannerErrorModel(DocumentScannerErrorModel.ErrorMessage.INVALID_IMAGE))
                    Handler(Looper.getMainLooper()).post{
                        finishActivity()
                    }
                }
            }
        }
    }

    override fun scanSurfaceShowProgress() {
        showProgressBar()
    }

    override fun scanSurfaceHideProgress() {
        hideProgressBar()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        if(isAdded) {
            getScanActivity().onError(error)
        }
    }

    override fun showFlashModeOn() {
        flashButton.setImageResource(R.drawable.zdc_flash_on)
    }

    override fun showFlashModeOff() {
        flashButton.setImageResource(R.drawable.zdc_flash_off)
    }
}