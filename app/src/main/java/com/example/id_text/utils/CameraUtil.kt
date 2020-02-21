package com.example.id_text.utils

import android.app.Activity
import androidx.core.os.EnvironmentCompat
import android.os.Environment.MEDIA_MOUNTED
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author qzns木雨
 * @email yingfeng.li@qq.com
 * @date on 2020/1/4
 * @describe 用于启动保存相机图片，获取拍摄图片
 */
object CameraUtil {

    //用于保存拍照图片的uri
    private var mCameraUri: Uri? = null

    //用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private var mCameraImagePath: String? = null

    // 是否是Android 10以上手机
    private val isAndroidQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    //获取相机图片的绝对路径
    fun getImagePath(context: Context) = if (isAndroidQ) {
        UriUtil.getFilePathByUri(context, mCameraUri)
    } else {
        mCameraImagePath
    }

    /**
     * 调起相机拍照
     */
    fun openCamera(activity: Activity, requestCode: Int): Boolean {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        captureIntent.resolveActivity(activity.packageManager) ?: return false

        // 判断是否有相机
        var photoFile: File? = null
        var photoUri: Uri? = null

        if (isAndroidQ) {
            // 适配android 10
            photoUri = createImageUri(activity)
        } else {
            try {
                photoFile = createImageFile(activity)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (photoFile != null) {
                mCameraImagePath = photoFile.absolutePath
                photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
                    FileProvider.getUriForFile(
                        activity,
                        activity.packageName + ".fileprovider",
                        photoFile
                    )
                } else {
                    Uri.fromFile(photoFile)
                }
            }
        }

        return if (photoUri != null) {
            mCameraUri = photoUri
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            activity.startActivityForResult(captureIntent, requestCode)
            true
        } else {
            false
        }
    }


    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(activity: Activity): Uri? {
        val status = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == MEDIA_MOUNTED) {
            activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )
        } else {
            activity.contentResolver.insert(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                ContentValues()
            )
        }
    }

    /**
     * 创建保存图片的文件
     */
    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File? {
        val imageName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        storageDir ?: return null

        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val tempFile = File(storageDir, imageName)
        return if (MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }


}