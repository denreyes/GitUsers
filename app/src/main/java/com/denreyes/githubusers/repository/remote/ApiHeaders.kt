package com.denreyes.githubusers.repository.remote

import com.denreyes.githubusers.BuildConfig
import java.util.*

class ApiHeaders private constructor() {

	val headers: Map<String?, String?>
		get() {
			val headers: MutableMap<String?, String?> = HashMap()
			headers["Authorization"] = BuildConfig.AUTH_TOKEN
			return headers
		}

	companion object {
		var instance: ApiHeaders? = null
			private set

		fun init() {
			instance = ApiHeaders()
		}
	}
}
