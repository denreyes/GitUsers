package com.denreyes.githubusers.repository.remote

import android.content.Context

class GithubApi private constructor(context: Context, val baseUrl: String) {
	var isDebug = false
	var connectionTimeout = 5L
	var writeTimeout = 5L
	var readTimeout = 5L

	companion object {
		var instance: GithubApi? = null
			private set

		fun init(context: Context, baseUrl: String) {
			instance = GithubApi(context, baseUrl)
			ApiConnector.init()
		}

	}

}
