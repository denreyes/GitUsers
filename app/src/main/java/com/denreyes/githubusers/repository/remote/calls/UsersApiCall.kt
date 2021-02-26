package com.denreyes.githubusers.repository.remote.calls

import com.denreyes.githubusers.repository.remote.ApiConnector
import com.denreyes.githubusers.repository.remote.ApiHeaders
import com.denreyes.githubusers.repository.remote.ApiRequestListener
import com.denreyes.githubusers.repository.remote.handlers.UsersHandler
import okhttp3.ResponseBody
import retrofit2.Call

class UsersApiCall : BaseCall() {
	private val handler: UsersHandler = ApiConnector.instance!!.createService(UsersHandler::class.java)

	fun getUsers(perPage: Int, since: Int, listener: ApiRequestListener?) {
		val call: Call<ResponseBody?> = handler.getUsers(ApiHeaders.instance!!.headers, perPage.toString(), since.toString())!!
		call(call, listener)
	}

	fun getUser(username: String, listener: ApiRequestListener?) {
		val call: Call<ResponseBody?> = handler.getUser(ApiHeaders.instance!!.headers, username)!!
		call(call, listener)
	}
}
