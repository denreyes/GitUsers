package com.denreyes.githubusers.repository.remote.handlers

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UsersHandler {

	@GET("users")
	fun getUsers(@HeaderMap headers: Map<String?, String?>, @Query("per_page") perPage: String, @Query("since") since: String): Call<ResponseBody?>?

	@GET("users/{username}")
	fun getUser(@HeaderMap headers: Map<String?, String?>, @Path("username") username: String): Call<ResponseBody?>?

}
