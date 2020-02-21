package com.example.id_text.data.model

data class TextResult(var log_id: Long) {

    //字段数目
    var words_result_num = 0

    //字段结果
    var words_result = mutableListOf<SingleText>()

    //置信度，非必须
    var probability = 0F


    //单句对象
    data class SingleText(var words: String) {
        var count = 0
    }
}