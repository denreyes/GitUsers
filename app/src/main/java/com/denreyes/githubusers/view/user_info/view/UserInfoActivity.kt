package com.denreyes.githubusers.view.user_info.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.denreyes.githubusers.R
import com.denreyes.githubusers.view.user_info.UserInfoViewModel
import kotlinx.android.synthetic.main.activity_user_info.*

class UserInfoActivity : AppCompatActivity() {
	private var mId: Int = 0
	private var viewModel: UserInfoViewModel? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_user_info)
		mId = intent?.getIntExtra("ID", 0)!!

		setupViews()
		viewModel?.fetchLocalUser(mId)
		viewModel?.fetchUserInfo(mId)
		listenToNetwork()
	}


	fun setupViews() {
		iv_back.setOnClickListener { onBackPressed() }

		viewModel = ViewModelProviders.of(this).get(UserInfoViewModel::class.java)
		viewModel?.fetchLocalUser(mId)
		viewModel?.getUserObserver()?.observe(this@UserInfoActivity, Observer {

			Glide.with(this)
				.load(it.avatarUrl)
				.into(iv_profile)

			tv_toolbar_title.text = it.name
			tv_followers.text = "followers ${it.followers}"
			tv_following.text = "following ${it.following}"
			tv_name.text = "name: ${it.name}"

			if (it.company != null && it.company.isNotEmpty()) {
				tv_company.visibility = View.VISIBLE
				tv_company.text = "company: ${it.company}"
			} else
				tv_company.visibility = View.GONE

			if (it.blog != null && it.blog.isNotEmpty()) {
				tv_blog.visibility = View.VISIBLE
				tv_blog.text = "blog: ${it.blog}"
			} else
				tv_blog.visibility = View.GONE

			if(it.note.isNotEmpty())
				et_notes.setText(it.note)
		})
		btn_save.setOnClickListener {
			val note = et_notes.text.toString()
			viewModel?.saveNote(mId, note)
			Toast.makeText(this@UserInfoActivity, getString(R.string.note_added), Toast.LENGTH_LONG).show()
		}
		viewModel?.getHasConnectionObserver()?.observe(this@UserInfoActivity, Observer {
			tv_connection_unavailable.visibility = if (it) View.GONE else View.VISIBLE
			if (it) {
				viewModel?.fetchUserInfo(mId)
			}
		})
	}

	fun listenToNetwork() {
		val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
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

}
