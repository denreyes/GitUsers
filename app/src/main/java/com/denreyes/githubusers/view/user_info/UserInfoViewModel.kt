package com.denreyes.githubusers.view.user_info

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.denreyes.githubusers.repository.remote.ApiRequestListener
import com.denreyes.githubusers.repository.remote.calls.UsersApiCall
import com.denreyes.githubusers.repository.remote.models.User
import com.denreyes.githubusers.repository.local.RoomAppDb
import com.denreyes.githubusers.repository.local.UserEntity
import com.denreyes.githubusers.util.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException


class UserInfoViewModel(app: Application) : AndroidViewModel(app) {
	private var user : MutableLiveData<UserEntity>
	private var isLoading = MutableLiveData<Boolean>()
	private var hasConnection = MutableLiveData<Boolean>()

	init {
		user = MutableLiveData()
		isLoading = MutableLiveData(false)
		hasConnection = MutableLiveData(true)
	}

	fun getUserObserver(): MutableLiveData<UserEntity> {
		return user
	}

	fun getIsLoadingObserver(): MutableLiveData<Boolean> {
		return isLoading
	}

	fun getUser(id: Int) : UserEntity? {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		return userDao?.getUser(id)
	}

	fun postUser(id: Int) {
		user.postValue(getUser(id))
	}

	fun updateUser(entity: UserEntity) {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		userDao?.updateUser(entity)
		postUser(entity.id)
	}

	fun getHasConnectionObserver(): MutableLiveData<Boolean> {
		return hasConnection
	}

	fun setHasConnection(online: Boolean) {
		hasConnection.postValue(online)
	}

	fun fetchUserInfo(id: Int) {
		if (!isNetworkAvailable()) {
			isLoading.postValue(false)
			hasConnection.postValue(false)
			return
		}

		isLoading.value = true

		UsersApiCall().getUser(getUser(id)?.login!!, object : ApiRequestListener {
			override fun onSuccess(o: Any) {
				try {
					val it: User = Gson().fromJson(o.toString(), object : TypeToken<User?>() {}.type)

					val login = if (it.login == null) "" else it.login
					val avatarUrl = if (it.avatarUrl == null) "" else it.avatarUrl
					val type = if (it.type == null) "" else it.type
					val name = if (it.name == null) "" else it.name
					val company = if (it.company == null) "" else it.company
					val blog = if (it.blog == null) "" else it.blog
					val followers = if (it.followers == null) 0 else it.followers
					val following = if (it.following == null) 0 else it.following
					val note = if (user.value?.note == null)  "" else user.value?.note!!

					val user = UserEntity(
						it.id, login,
						avatarUrl, type, name, company, blog,
						followers, following, note
					)
					updateUser(user)
					isLoading.value = false
				} catch (e: JSONException) {
					onError(e)
				}
			}

			override fun onError(o: Any) {
				isLoading.value = false
				AppLogger.printRetrofitError(o)
			}
		})
	}

	fun saveNote(id: Int, note: String) {
		val entity = getUser(id)!!
		val updateEnt = UserEntity(
			entity.id, entity.login,
			entity.avatarUrl, entity.type,
			entity.name, entity.company,
			entity.blog, entity.followers,
			entity.following, note
		)
		updateUser(updateEnt)
	}

	fun fetchLocalUser(id: Int) {
		postUser(id)
	}

	private fun isNetworkAvailable(): Boolean {
		var haveConnectedWifi = false
		var haveConnectedMobile = false

		val cm = (getApplication() as Context).getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
		val netInfo = cm!!.allNetworkInfo
		for (ni in netInfo) {
			if (ni.typeName.equals(
					"WIFI",
					ignoreCase = true
				)
			) if (ni.isConnected) haveConnectedWifi = true
			if (ni.typeName.equals(
					"MOBILE",
					ignoreCase = true
				)
			) if (ni.isConnected) haveConnectedMobile = true
		}
		return haveConnectedWifi || haveConnectedMobile
	}

}
