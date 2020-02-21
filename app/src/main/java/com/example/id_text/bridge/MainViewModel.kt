package com.example.id_text.bridge

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.id_text.data.TIP_LOADING
import com.example.id_text.data.TIP_SHOW
import com.example.id_text.data.TextRepository
import com.example.id_text.data.model.TextResult
import com.example.id_text.utils.GsonUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val repository: TextRepository) : ViewModel() {

    //识别的文字
    var idText = MutableLiveData<String>()

    //是否输出
    var isOutput = MutableLiveData<Boolean>()

    //提示文字
    var tipText = MutableLiveData<String>()

    //初始化TableLayout和ViewPager
    var initTabAndPage = MutableLiveData<Boolean>()

    //存有字段统计的list
    var textList = MutableLiveData<List<Pair<String, Int>>>()


    //识别图片为文字
    fun identifyText(picPath: String) {

        idText.value = ""
        isOutput.value = false
        tipText.value = TIP_LOADING
        textList.value = listOf()

        GlobalScope.launch {
            //解析
            val textResult = repository.identify(picPath)
            val listResult = repository.getTextList()

            //更新LiveData从而更新UI
            isOutput.postValue(true)
            tipText.postValue(TIP_SHOW)

            idText.postValue(textResult)
            textList.postValue(listResult)
        }
    }
}