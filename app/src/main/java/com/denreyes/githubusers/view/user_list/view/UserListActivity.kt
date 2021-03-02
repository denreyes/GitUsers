package com.denreyes.githubusers.view.user_list.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.denreyes.githubusers.R
import com.denreyes.githubusers.repository.local.UserEntity
import com.denreyes.githubusers.view.user_info.view.UserInfoActivity
import com.denreyes.githubusers.view.user_list.UserListViewModel
import com.denreyes.githubusers.view.user_list.adapter.UserListRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.layout_list_placeholder.*


class UserListActivity : AppCompatActivity(), UserListRecyclerViewAdapter.UserListAdapterListener {
	private var viewModel: UserListViewModel? = null
	private var usersAdapter: UserListRecyclerViewAdapter? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_user_list)

		setupViews()
		listenToNetwork()
		if (et_search.text.toString().isEmpty())
			viewModel?.fetchUsers(0)
	}

	override fun onResume() {
		super.onResume()
		refresh()
	}

	fun setupViews() {
		rv_users.apply {
			layoutManager = LinearLayoutManager(this@UserListActivity)
			usersAdapter = UserListRecyclerViewAdapter(this@UserListActivity, this@UserListActivity)
			adapter = usersAdapter
		}
		viewModel = ViewModelProviders.of(this).get(UserListViewModel::class.java)
		viewModel?.getAllUserObservers()?.observe(this@UserListActivity, { it ->
			if (et_search.text.toString().isEmpty()) {
				if (it?.isNotEmpty()!!) {
					tv_empty.visibility = View.GONE
					rv_users.visibility = View.VISIBLE
					usersAdapter?.setListData(ArrayList(it))
					usersAdapter?.notifyDataSetChanged()
				} else {
					tv_empty.visibility = View.VISIBLE
					rv_users.visibility = View.INVISIBLE
				}
			}
		})
		viewModel?.getSearchUsersObservers()?.observe(this@UserListActivity, { it ->
			if (it?.isNotEmpty()!!) {
				tv_empty.visibility = View.GONE
				rv_users.visibility = View.VISIBLE
				usersAdapter?.setListData(ArrayList(it))
				usersAdapter?.notifyDataSetChanged()
			} else {
				tv_empty.visibility = View.VISIBLE
				rv_users.visibility = View.INVISIBLE
			}
		})
		viewModel?.getIsLoadingObserver()?.observe(this@UserListActivity, Observer {
			setupLoadViews(it)
		})
		viewModel?.getHasConnectionObserver()?.observe(this@UserListActivity, Observer {
			tv_connection_unavailable.visibility = if (it) View.GONE else View.VISIBLE
			if (it && et_search.text.toString().isEmpty()) {
				val lastItem = usersAdapter?.getLastItem()
				val since = lastItem?.id ?: 0
				viewModel?.fetchUsers(since)
			}
		})

		et_search.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
			}

			override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
			}

			override fun afterTextChanged(s: Editable?) {
				if (s!!.isNotEmpty()) {
					iv_clear.visibility = View.VISIBLE
					viewModel?.searchUser(s.toString())
				} else {
					iv_clear.visibility = View.GONE
					refresh()
				}
			}
		})

		swipe_refresh.setOnRefreshListener {
			if (et_search.text.isNotEmpty())
				swipe_refresh.isRefreshing = false
			else
				viewModel?.fetchUsers(0)
		}

		iv_clear.setOnClickListener {
			et_search.setText("")
			refresh()
		}
	}

	private fun setupLoadViews(isLoading: Boolean) {
		if (!isLoading) {
			swipe_refresh.isRefreshing = false
			usersAdapter?.setIsLoading(false)
			layout_list_placeholder.visibility = View.GONE
		} else {
			if (usersAdapter?.itemCount!! < 1)
				layout_list_placeholder.visibility = View.VISIBLE
			if (usersAdapter?.itemCount!! > 1)
				usersAdapter?.setIsLoading(true)
		}
	}

	fun listenToNetwork() {
		val networkCallback: NetworkCallback = object : NetworkCallback() {
			override fun onAvailable(network: Network) {
				viewModel?.setHasConnection(true)
			}

			override fun onLost(network: Network) {
				viewModel?.setHasConnection(false)
			}
		}

		val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			connectivityManager.registerDefaultNetworkCallback(networkCallback)
		} else {
			val request = NetworkRequest.Builder()
				.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
			connectivityManager.registerNetworkCallback(request, networkCallback)
		}
	}

	override fun onSelectUserListener(user: UserEntity) {
		val i = Intent(this, UserInfoActivity::class.java)
		i.putExtra("ID", user.id)
		startActivity(i)
	}

	override fun invertBitmap(resource: Bitmap): Bitmap? {
		return viewModel?.invertBitmap(resource)
	}

	override fun lastPositionVisible(user: UserEntity) {
		if (et_search.text.toString().isEmpty())
			viewModel?.fetchUsers(user.id)
	}

	fun refresh() {
		viewModel?.fetchLocalUsers(et_search.text.toString())
	}
}
