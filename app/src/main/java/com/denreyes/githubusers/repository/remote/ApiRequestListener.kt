package com.denreyes.githubusers.repository.remote

interface ApiRequestListener {
	fun onSuccess(obj: Any)
	fun onError(obj: Any)
}
