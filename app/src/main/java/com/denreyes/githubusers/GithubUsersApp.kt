package com.denreyes.githubusers

import android.app.Application
import com.denreyes.githubusers.repository.remote.ApiHeaders
import com.denreyes.githubusers.repository.remote.GithubApi

class GithubUsersApp : Application() {

	override fun onCreate() {
		super.onCreate()
		ApiHeaders.init()
		GithubApi.init(this, BuildConfig.GITHUB_BASE_URL)
	}

}
