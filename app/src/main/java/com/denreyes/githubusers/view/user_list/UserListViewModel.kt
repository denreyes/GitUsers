package com.denreyes.githubusers.view.user_list

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.denreyes.githubusers.repository.local.RoomAppDb
import com.denreyes.githubusers.repository.local.UserEntity
import com.denreyes.githubusers.repository.remote.ApiRequestListener
import com.denreyes.githubusers.repository.remote.calls.UsersApiCall
import com.denreyes.githubusers.repository.remote.models.User
import com.denreyes.githubusers.util.AppLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException


class UserListViewModel(app: Application) : AndroidViewModel(app) {
	private var allUsers : MutableLiveData<List<UserEntity>>
	private var searchUsers : MutableLiveData<List<UserEntity>>
	private var isLoading = MutableLiveData<Boolean>()
	private var hasConnection = MutableLiveData<Boolean>()
	private var perPage: Int = 0

	init {
		allUsers = MutableLiveData()
		searchUsers = MutableLiveData()
		isLoading = MutableLiveData(false)
		hasConnection = MutableLiveData(true)
	}

	fun getAllUserObservers(): MutableLiveData<List<UserEntity>> {
		return allUsers
	}

	fun getSearchUsersObservers(): MutableLiveData<List<UserEntity>> {
		return searchUsers
	}

	fun getIsLoadingObserver(): MutableLiveData<Boolean> {
		return isLoading
	}

	fun getHasConnectionObserver(): MutableLiveData<Boolean> {
		return hasConnection
	}

	fun setHasConnection(online: Boolean) {
		hasConnection.postValue(online)
	}

	fun getUsers(): List<UserEntity>? {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		return userDao?.getAllUsers()
	}

	fun getSearchedUsers(query: String): List<UserEntity>? {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		return userDao?.searchUsers("%$query%")
	}

	fun getUser(id: Int) : UserEntity? {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		return userDao?.getUser(id)
	}

	fun insertUser(entity: UserEntity) {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		val existingUser = getUser(entity.id)
		if (existingUser != null) {
			val updateEnt = UserEntity(
				entity.id, entity.login,
				entity.avatarUrl, entity.type,
				existingUser.name, existingUser.company,
				existingUser.blog, existingUser.followers,
				existingUser.following, existingUser.note
			)
			updateUser(updateEnt)
		} else {
			userDao?.insertUser(entity)
		}
	}

	fun deleteUser(entity: UserEntity) {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		userDao?.deleteUser(entity)
	}

	fun updateUser(entity: UserEntity) {
		val userDao = RoomAppDb.getAppDatabase(getApplication())?.userDao()
		userDao?.updateUser(entity)
	}

	fun fetchLocalUsers(query: String) {
		if (query.isEmpty())
			allUsers.postValue(getUsers())
		else
			searchUser(query)
	}

	fun fetchUsers(since: Int) {
		if (!isNetworkAvailable()) {
			isLoading.postValue(false)
			hasConnection.postValue(false)
			return
		}

		isLoading.postValue(true)

		UsersApiCall().getUsers(perPage, since, object : ApiRequestListener {
			override fun onSuccess(o: Any) {
				try {
					val users: List<User> = Gson().fromJson(
						o.toString(),
						object : TypeToken<List<User?>?>() {}.type
					)
					isLoading.postValue(false)
					users?.forEach {
						val user = UserEntity(
							it.id, it.login,
							it.avatarUrl, it.type, it.name, it.company, it.blog,
							it.followers, it.following, ""
						)
						insertUser(user)
						allUsers.postValue(getUsers())
					}
					if (since == 0)
						perPage = users.size

				} catch (e: JSONException) {
					onError(e)
				}
			}

			override fun onError(o: Any) {
				isLoading.postValue(false)
				AppLogger.printRetrofitError(o)
			}
		})
	}

	fun searchUser(query: String) {
		val users = getSearchedUsers(query)
		if(users != null)
			searchUsers.postValue(users)
	}

	fun invertBitmap(bmp: Bitmap): Bitmap {
		val RGB_MASK = 0x00FFFFFF;
		val inversion = bmp.copy(Bitmap.Config.ARGB_8888, true)

		val width = inversion.width
		val height = inversion.height
		val pixels = width * height

		val pixel = IntArray(pixels)
		inversion.getPixels(pixel, 0, width, 0, 0, width, height)

		for (i in 0 until pixels) pixel[i] = pixel[i] xor RGB_MASK
		inversion.setPixels(pixel, 0, width, 0, 0, width, height)

		return inversion
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
