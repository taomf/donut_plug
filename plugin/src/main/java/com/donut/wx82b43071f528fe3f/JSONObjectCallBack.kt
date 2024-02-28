package com.donut.wx82b43071f528fe3f

import com.zhy.http.okhttp.callback.Callback
import okhttp3.Response
import org.json.JSONObject

/**
 *    @author : taomf
 *    Date    : 2023/11/18/10:49
 *    Desc    :
 */
abstract class JSONObjectCallBack : Callback<JSONObject>() {
    override fun parseNetworkResponse(response: Response?, id: Int): JSONObject {
        val string = response!!.body().string()

        return JSONObject(string)
    }
}