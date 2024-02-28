package com.donut.wx82b43071f528fe3f

import android.app.Activity
import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.tencent.luggage.sdk.wxa_ktx.JSONUtils.toMap
import com.tencent.luggage.wxa.SaaA.plugin.AsyncJsApi
import com.tencent.luggage.wxa.SaaA.plugin.NativePluginInterface
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.https.HttpsUtils
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class TestNativePlugin: NativePluginInterface {
    private val TAG = "TestNativePlugin"
    private val GET = "GET"
    private val POST = "POST"
    private val FILE = "uploadFile"

    private var timeOut = 10
    private var port = 8089

    companion object{
        var orgName  = ""
        var orgId = ""
        var projectId = ""
        /** 分平台地址 **/
        var PlatformAddress = ""
    }


    private val regex = """^(http://|https://)(([0-9]{1,3}\.){3}[0-9]{1,3}|[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)*)(:\d{2,5})?(/[a-zA-Z0-9]+)*$""".toRegex()

    private var server: Server? = null

    init {
        val sslParams = HttpsUtils.getSslSocketFactory(null, null, null)
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //其他配置
            .readTimeout(timeOut.toLong(),TimeUnit.SECONDS)
            .writeTimeout(timeOut.toLong(),TimeUnit.SECONDS)
            .build()
        OkHttpUtils.initClient(okHttpClient)
    }

    override fun getPluginID(): String {
        Log.e(TAG, "getPluginID")
        return BuildConfig.PLUGIN_ID
    }

    /**
     * 开启web服务
     */
    @AsyncJsApi(methodName = "startWebService")
    fun startService(data: JSONObject?,callback: (data: String) -> Unit,context: Activity) {
        Log.i(TAG, "收到的参数：" + data.toString())

        orgName = data?.optString("orgName") ?:""
        orgId = data?.optString("orgId") ?:""
        projectId = data?.optString("projectId") ?:""
        PlatformAddress = data?.optString("PlatformAddress") ?: ""

        timeOut = data?.optInt("timeOut",timeOut) ?: timeOut
        port = data?.optInt("port") ?: port


        server = AndServer.webServer(context).port(port).timeout(timeOut, TimeUnit.SECONDS)
            .listener(object : Server.ServerListener {
                override fun onStarted() {
                    callback("服务开启")
                }

                override fun onStopped() {
                    callback("服务关闭")
                }

                override fun onException(e: Exception) {
                    callback(e.toString())
                }
            }).build()

        server?.startup()
    }

    /**
     * 关闭web服务
     */
    @AsyncJsApi(methodName = "stopWebService")
    fun stopService() {
        server?.shutdown()
    }


    @AsyncJsApi(methodName = "myRequest")
    fun request(data: JSONObject?, callback: (data: Any) -> Unit) {
        Log.i(TAG, "收到的参数：" + data.toString())

        val url = data?.optString("url")
        timeOut  = data?.optInt("timeout") ?: 10
        val method = data?.optString("method")
        val fileUrl = data?.optString("fileUrl")
        val params  = data?.optJSONObject("param")
        val headers = data?.optJSONObject("header").takeIf { it != null }?.toMap() as MutableMap<String, String>?

        when{
            url.isNullOrBlank() ->{
                ToastUtils.showShort("地址不能为空!")
                return
            }
            method.isNullOrBlank() || (method != GET && method != POST && method != FILE) ->{
                ToastUtils.showShort("请求方式错误")
                return
            }
            method == FILE && (fileUrl.isNullOrBlank() || !File(fileUrl).exists()) ->{
                ToastUtils.showShort("文件不能为空！")
                return
            }
        }

        when (method) {
            GET -> {
                Log.i(TAG, "GET请求 params参数$params")

                OkHttpUtils.get()
                    .url(url)
                    .headers(headers)
                    .params(params.takeIf { it != null }?.toMap() as MutableMap<String, String>?)
                    .build()
                    .execute(object : JSONObjectCallBack() {
                        override fun onError(call: Call?, e: Exception?, id: Int) {
                            Log.i(TAG, e.toString())

                            callback(e.toString())
                        }

                        override fun onResponse(response: JSONObject?, id: Int) {
                            Log.i(TAG, response.toString())

                            callback(response!!)
                        }

                    })
            }
            POST -> {
                Log.i(TAG, "POST请求 params参数$params")

                OkHttpUtils.postString()
                    .url(url)
                    .headers(headers)
                    .content(params.toString())
                    .mediaType(MediaType.parse("application/json; charset=utf-8"))
                    .build().execute(object : JSONObjectCallBack() {
                        override fun onError(call: Call?, e: Exception?, id: Int) {
                            Log.e(TAG, e.toString())

                            callback(e.toString())
                        }

                        override fun onResponse(response: JSONObject?, id: Int) {
                            Log.i(TAG, response.toString())

                            callback(response!!)
                        }
                    })
            }
            FILE -> {
                OkHttpUtils
                    .postFile()
                    .url(url)
                    .file(File(fileUrl))
                    .build()
                    .execute(object : JSONObjectCallBack() {
                        override fun onError(call: Call?, e: Exception?, id: Int) {
                            Log.e(TAG, e.toString())

                            callback(e.toString())
                        }

                        override fun onResponse(response: JSONObject?, id: Int) {
                            callback(response!!)
                        }

                        override fun inProgress(progress: Float, total: Long, id: Int) {
                            super.inProgress(progress, total, id)

                        }
                    })
            }
        }
    }

}

