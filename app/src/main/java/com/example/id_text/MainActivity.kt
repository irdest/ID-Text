package com.example.id_text

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.id_text.bridge.MainViewModel
import android.provider.MediaStore
import android.view.KeyEvent
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import com.example.id_text.adapter.RecyclerAdapter
import com.example.id_text.base.BaseActivity
import com.example.id_text.bridge.Injector
import com.example.id_text.data.TIP_START
import com.example.id_text.data.model.TextResult
import com.example.id_text.databinding.ActivityMainBinding
import com.example.id_text.utils.CameraUtil
import com.example.id_text.utils.UriUtil
import me.yifeiyuan.pandora.ToastUtils


class MainActivity : BaseActivity() {

    //初始化viewModel 和 databinding
    private val viewModel by lazy {
        ViewModelProvider(this, Injector.getMainFactory()).get(MainViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    //文本列表
    private val textList by lazy {
        mutableListOf<Pair<String, Int>>()
    }

    //常量
    companion object {
        //权限
        private const val RC_PERMISSION = 101
        private val PERMISSIONS = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        //相册、拍照相关
        private const val RC_ALBUM = 2
        private const val RC_CAMERA = 3

        private val TAG = MainActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        //Databinding双向绑定数据
        binding.lifecycleOwner = this
        binding.vm = viewModel
        binding.click = ClickProxy()


        viewModel.tipText.value = TIP_START
        viewModel.isOutput.value = false

        //这里的initTabAndPage 是通过bindingAdapter关联tableLayout、viewpager
        viewModel.initTabAndPage.value = true

        //设置字数统计的Rv
        val adapter = RecyclerAdapter(textList)
        binding.textRv.adapter = adapter
        //文本列表的变动
        viewModel.textList.observe(this, Observer<List<Pair<String, Int>>> {
            Log.d(TAG, "textList observe")
            textList.clear()
            textList.addAll(it)
            adapter.notifyDataSetChanged()
        })
        viewModel.textList.value = listOf()
    }

    //权限申请
    private fun requestPermissions() {
        val requestPermissions = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (requestPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                requestPermissions.toTypedArray(),
                RC_PERMISSION
            )
        } else {
            Log.d(TAG, "requestPermissions: permissions is OK")
        }
    }

    //权限回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSION -> {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "onRequestPermissionsResult is not GRANTED")
                        finish()
                    }
                }
            }

            else -> Log.e(TAG, "onRequestPermissionsResult erro")
        }
    }

    //Databinding的点击代理
    inner class ClickProxy {

        //调用相机
        fun onCameraClick() {
            Log.d(TAG, "onCameraClick")
            CameraUtil.openCamera(this@MainActivity, RC_CAMERA)
        }

        //调用相册
        fun onAlbumClick() {
            Log.d(TAG, "onAlbumClick")
            val intentToPickPic = Intent(Intent.ACTION_PICK, null)
            intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intentToPickPic, RC_ALBUM)
        }

        //copy
        fun copyAll() {
            val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(null, viewModel.idText.value)

            cmb.setPrimaryClip(clipData)
            ToastUtils.show("已复制到剪贴板")
        }
    }

    //图片获取结果回调
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: $resultCode")

        if (resultCode != RESULT_OK) {
            return
        }

        val picPath = when (requestCode) {
            RC_CAMERA -> {
                Log.d(TAG, "Camera Callback")
                CameraUtil.getImagePath(this)
            }

            RC_ALBUM -> {
                Log.d(TAG, "ALBUM Callback")
                UriUtil.getFilePathByUri(this, data?.data)
            }

            else -> ""
        }

        if (!picPath.isNullOrBlank()) {
            viewModel.identifyText(picPath)
        }
    }

    //返回键处理
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (isSureToExitAfterDoubleClick) {
                    return true
                }
            }
        }
        finish()
        return true
    }
}
