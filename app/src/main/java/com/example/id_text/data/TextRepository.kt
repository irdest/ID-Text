package com.example.id_text.data

import android.util.Log
import com.example.id_text.data.model.TextResult
import com.example.id_text.utils.HttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.id_text.utils.Base64Util
import com.example.id_text.utils.FileUtil
import com.example.id_text.utils.GsonUtils
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TextRepository private constructor() {

    private val accessToken = getAuth(API_KEY, SECRET_KEY)

    //默认精度
    private val isAccurate = true

    //数据
    private var resultBean = TextResult(0)

    companion object {
        val instance: TextRepository by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TextRepository()
        }

        const val URL_IDENTITY_TEXT = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic"

        //高精度，每日仅500条请求流量
        const val URL_IDENTITY_TEXT_ACCURATE =
            "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic"

        const val API_KEY = "q4OiFQFk4NWEt8jF3ATWrWmz"

        const val SECRET_KEY = "ECVaMewUric2736GsBXiAYKmflNeyVLC"

        private val TAG = TextRepository::class.simpleName
    }

    //获取证书
    private fun getAuth(ak: String, sk: String): String = runBlocking {

        Log.d(TAG, "getAuth")
        withContext(Dispatchers.IO) {
            // 获取token地址
            val authHost = "https://aip.baidubce.com/oauth/2.0/token?"
            val getAccessTokenUrl = (authHost
                    // 1. grant_type为固定参数
                    + "grant_type=client_credentials"
                    // 2. 官网获取的 API Key
                    + "&client_id=" + ak
                    // 3. 官网获取的 Secret Key
                    + "&client_secret=" + sk)

            try {
                val realUrl = URL(getAccessTokenUrl)
                // 打开和URL之间的连接
                val connection = realUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                // 获取所有响应头字段
                val map = connection.headerFields
                // 遍历所有的响应头字段
                for (key in map.keys) {
                    System.err.println(key + "--->" + map[key])
                }

                // 定义 BufferedReader输入流来读取URL的响应
                val inBuffer = BufferedReader(InputStreamReader(connection.inputStream))
                var result = ""
                var line: String?

                while (true) {
                    line = inBuffer.readLine()

                    if (line == null) {
                        break
                    } else {
                        result += line
                    }
                }

                /**
                 * 返回结果示例
                 */
                System.err.println("result:$result")
                val jsonObject = JSONObject(result)

                jsonObject.getString("access_token")
            } catch (e: Exception) {
                System.err.printf("获取token失败！")
                e.printStackTrace(System.err)
                ""
            }
        }
    }

    //通过图片识别文字
    suspend fun identify(picPath: String) = withContext(Dispatchers.IO) {

        val url = if (isAccurate) {
            URL_IDENTITY_TEXT_ACCURATE
        } else {
            URL_IDENTITY_TEXT
        }

        val imgData = FileUtil.readFileByBytes(picPath)
        val imgStr = Base64Util.encode(imgData)
        val imgParam = URLEncoder.encode(imgStr, "UTF-8")
        val param = "image=$imgParam"

        val textJson = HttpUtil.post(url, accessToken, param)

        resultBean = GsonUtils.fromJson<TextResult>(textJson, TextResult::class.java)

        var resultStr = ""
        resultBean.words_result.forEach {
            resultStr += it.words + "\n"
        }

        resultStr
    }

    //获取字段信息
    suspend fun getTextList() = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, Int>()

        resultBean.words_result.forEach {
            if (map.containsKey(it.words)) {
                map[it.words] = map[it.words]!! + 1
            } else {
                map[it.words] = 1
            }
        }

        map.toList().sortedByDescending { it.second }
    }
}