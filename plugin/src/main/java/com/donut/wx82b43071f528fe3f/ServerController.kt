package com.donut.wx82b43071f528fe3f

import com.alibaba.fastjson.JSONObject
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.RestController
import com.zhy.http.okhttp.OkHttpUtils
import okhttp3.Call
import okhttp3.MediaType


/**
 * @author : taomf
 * Date    : 2023/10/11/14:14
 * Desc    :
 */
@RestController
class ServerController {
    var log = "TaomfController"
    var addApi = "/pm/v2/device/add"

    @GetMapping("/getPlatformAddress")
    fun getPlatformAddress(): String {
        val jsonObject = JSONObject().apply {
            this["msgInfo"] = "接口调用成功!"
            this["msgCode"] = "200"
            this["result"] = TestNativePlugin.PlatformAddress
        }
        return jsonObject.toString()
    }

    @GetMapping("/addDevice")
    fun addDevice(
        @RequestParam("deviceCode") deviceCode: String,
        @RequestParam("deviceType") deviceType: String,
        @RequestParam("ip") ip: String,
        @RequestParam("deviceVersion", required = false)  deviceVersion: String
    ): String {

        when {
            deviceCode.isBlank() -> {
                return JSONObject().apply {
                    this["msgInfo"] = "设备号不能为空!"
                    this["msgCode"] = "400"
                    this["result"] = "添加失败"
                }.toString()
            }
            deviceType.isBlank() -> {
                return JSONObject().apply {
                    this["msgInfo"] = "设备类型不能为空!"
                    this["msgCode"] = "400"
                    this["result"] = "添加失败"
                }.toString()
            }
        }

        val data = JSONObject().also {
            it["orgName"] = TestNativePlugin.orgName
            it["orgId"] = TestNativePlugin.orgId
            it["deviceCode"] = deviceCode
            it["deviceType"] = deviceType
            it["projectId"] = TestNativePlugin.projectId
            it["ip"] = ip
            it["deviceVersion"] = deviceVersion
        }

        OkHttpUtils.postString()
            .url(TestNativePlugin.PlatformAddress + addApi)
            .content(data.toString())
            .mediaType(MediaType.parse("application/json; charset=utf-8"))
            .build().execute(object : JSONObjectCallBack() {
                override fun onError(call: Call?, e: Exception?, id: Int) {
                }
                override fun onResponse(response: org.json.JSONObject?, id: Int) {
                }
            })



        return JSONObject().apply {
            this["msgInfo"] = "接口调用成功!"
            this["msgCode"] = "200"
            this["result"] = ""
        }.toString()
    }
}