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

package com.krobys.documentscanner.manager

import android.content.Context
import android.graphics.Bitmap
import id.zelory.compressor.extension

internal class SessionManager(context: Context) {

    companion object {
        private const val IMAGE_SIZE_KEY = "IMAGE_SIZE_KEY"
        private const val IMAGE_QUALITY_KEY = "IMAGE_QUALITY_KEY"
        private const val IMAGE_TYPE_KEY = "IMAGE_TYPE_KEY"
        private const val IS_GALLERY_ENABLED = "IS_GALLERY_ENABLED"
        private const val IS_CROPPER_ENABLED = "IS_CROPPER_ENABLED"
        private const val IS_CAPTURE_MODE_BUTTON_ENABLED = "IS_CAPTURE_MODE_BUTTON_ENABLED"
        private const val IS_AUTO_CAPTURE_ENABLED_BY_DEFAULT = "IS_AUTO_CAPTURE_ENABLED_BY_DEFAULT"
        private const val IS_MAGIC_BUTTON_ENABLED = "IS_MAGIC_BUTTON_ENABLED"
        private const val IS_LIVE_DETECTION_ENABLED = "IS_LIVE_DETECTION_ENABLED"

        private const val DEFAULT_IMAGE_TYPE = "jpg"
    }
    private val preferences = context.getSharedPreferences("ZDC_Shared_Preferences", Context.MODE_PRIVATE)


    fun getImageSize(): Long {
        return preferences.getLong(IMAGE_SIZE_KEY, -1L)
    }

    fun setImageSize(size: Long) {
        preferences.edit().putLong(IMAGE_SIZE_KEY, size).apply()
    }

    fun getImageQuality(): Int {
        return preferences.getInt(IMAGE_QUALITY_KEY, 100)
    }

    fun setImageQuality(quality: Int) {
        preferences.edit().putInt(IMAGE_QUALITY_KEY, quality).apply()
    }

    fun getImageType(): Bitmap.CompressFormat {
        return compressFormat(preferences.getString(IMAGE_TYPE_KEY, DEFAULT_IMAGE_TYPE)!!)
    }

    fun setImageType(type: Bitmap.CompressFormat) {
        preferences.edit().putString(IMAGE_TYPE_KEY, type.extension()).apply()
    }

    private fun compressFormat(format: String) = when (format.toLowerCase()) {
        "png" -> Bitmap.CompressFormat.PNG
        "webp" -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.JPEG
    }

    fun isGalleryEnabled(): Boolean {
        return preferences.getBoolean(IS_GALLERY_ENABLED, true)
    }

    fun setGalleryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(IS_GALLERY_ENABLED, enabled).apply()
    }

    fun isCropperEnabled(): Boolean {
        return preferences.getBoolean(IS_CROPPER_ENABLED, true)
    }

    fun setCropperEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(IS_CROPPER_ENABLED, enabled).apply()
    }

    fun isCaptureModeButtonEnabled(): Boolean {
        return preferences.getBoolean(IS_CAPTURE_MODE_BUTTON_ENABLED, true)
    }

    fun setCaptureModeButtonEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(IS_CAPTURE_MODE_BUTTON_ENABLED, enabled).apply()
    }

    fun isAutoCaptureEnabledByDefault(): Boolean {
        return preferences.getBoolean(IS_AUTO_CAPTURE_ENABLED_BY_DEFAULT, true)
    }

    fun setAutoCaptureEnabledByDefault(enabled: Boolean) {
        preferences.edit().putBoolean(IS_AUTO_CAPTURE_ENABLED_BY_DEFAULT, enabled).apply()
    }

    fun isLiveDetectionEnabled(): Boolean {
        return preferences.getBoolean(IS_LIVE_DETECTION_ENABLED, true)
    }

    fun setLiveDetectionEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(IS_LIVE_DETECTION_ENABLED, enabled).apply()
    }

    fun isMagicButtonEnabled(): Boolean {
        return preferences.getBoolean(IS_MAGIC_BUTTON_ENABLED, true)
    }

    fun setMagicButtonEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(IS_MAGIC_BUTTON_ENABLED, enabled).apply()
    }
}
