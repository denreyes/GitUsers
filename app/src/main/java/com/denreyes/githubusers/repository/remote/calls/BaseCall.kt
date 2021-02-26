package com.denreyes.githubusers.repository.remote.calls

import com.denreyes.githubusers.repository.remote.ApiRequestListener
import com.denreyes.githubusers.util.AppLogger
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

open class BaseCall {

	private var call: Call<ResponseBody?>? = null

	protected fun call(call: Call<ResponseBody?>?, listener: ApiRequestListener?) {
		this.call = call
		this.call?.enqueue(object : Callback<ResponseBody?> {

			override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
 				try {
					val serverResponse: String?
					val jsonObject: JSONObject
					val jsonArray: JSONArray
					if (response.isSuccessful) {
						if (response.body() != null) {
							serverResponse = (response.body() as ResponseBody).string()
							try {
								jsonObject = JSONObject(serverResponse)
								AppLogger.printJson(jsonObject)
								listener?.onSuccess(jsonObject)
							} catch (var5 : JSONException) {
								try {
									jsonArray = JSONArray(serverResponse)
									AppLogger.printJson(jsonArray)
									listener?.onSuccess(jsonArray)
								} catch (var6 : JSONException) {
									var6.printStackTrace()
									listener?.onError(var6)
								}
							}
						} else {
							listener?.onSuccess("")
						}
					} else {
						serverResponse = response.errorBody()?.string()
						jsonObject = JSONObject(serverResponse)
						listener?.onError(jsonObject)
					}
				} catch (var5: IOException) {
					var5.printStackTrace()
					listener?.onError(var5)
				}
			}

			override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
				t!!.printStackTrace()
				listener?.onError(t)
			}
		})
	}

	fun forceStop() {
		if (call != null && call?.isExecuted!!) {
			call!!.cancel()
		}
	}
}
