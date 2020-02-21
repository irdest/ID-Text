package com.example.id_text.bridge

import com.example.id_text.data.TextRepository

//注入器，最小知识原则
object Injector {
    fun getMainFactory() = MainModelFactory(TextRepository.instance)
}