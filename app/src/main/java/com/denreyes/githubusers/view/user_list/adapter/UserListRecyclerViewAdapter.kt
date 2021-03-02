package com.denreyes.githubusers.view.user_list.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.denreyes.githubusers.R
import com.denreyes.githubusers.repository.local.UserEntity
import kotlinx.android.synthetic.main.item_user.view.*

class UserListRecyclerViewAdapter(private val listener: UserListAdapterListener, private val mContext: Activity) : Adapter<RecyclerView.ViewHolder>() {

	val ITEM = 0
	val PROGRESS_FOOTER = 1

	var items = ArrayList<UserEntity>()
	var isLoading = false

	fun setListData(data : ArrayList<UserEntity>) {
		this.items = data
	}

	override fun getItemViewType(position: Int): Int {
		return if (isLoading && position == itemCount - 1)
			PROGRESS_FOOTER
		else
			ITEM
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == ITEM)
			UserViewHolder(mContext, LayoutInflater.from(parent.context).inflate(
				R.layout.item_user, parent, false), listener)
		else
			ProgressFooterViewHolder(LayoutInflater.from(parent.context).inflate(
				R.layout.item_user_progress_footer, parent, false))
    }

	override fun getItemCount(): Int {
		return items.size + (if (isLoading) 1 else 0)
	}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if(holder is UserViewHolder) {
			if (position == itemCount - 1) {
				listener.lastPositionVisible(items[position])
			}
			holder.bind(items[position])
		} else if (holder is ProgressFooterViewHolder) {
			holder.bind()
		}
    }

	fun getLastItem(): UserEntity? {
		if (items.isNotEmpty()) {
			return items[items.size - 1]
		}
		return null
	}

	fun setIsLoading(loading : Boolean) {
		isLoading = loading
		notifyDataSetChanged()
	}

	class UserViewHolder(private val context: Activity, view: View, val listener: UserListAdapterListener): RecyclerView.ViewHolder(view) {
		fun bind(data: UserEntity) {
			itemView.tv_username.text = data.login
			itemView.tv_details.text = data.type
			itemView.iv_note.visibility = if (data.note.isNotEmpty()) View.VISIBLE else View.GONE

			if ((position + 1) % 4 == 0) {
				itemView.iv_profile.setImageResource(R.drawable.placeholder_octocat)
				Glide.with(context)
						.asBitmap()
						.load(data.avatarUrl)
						.into(object : CustomTarget<Bitmap>() {
							override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
								var res = listener.invertBitmap(resource)!!
								itemView.iv_profile.setImageBitmap(res)
							}

							override fun onLoadCleared(placeholder: Drawable?) {
							}
						})
			} else {
				Glide.with(context)
						.load(data.avatarUrl)
						.placeholder(R.drawable.placeholder_octocat)
						.error(R.drawable.placeholder_octocat)
						.into(itemView.iv_profile )
			}

			itemView.setOnClickListener {
				listener.onSelectUserListener(data)
			}
		}
	}

	class ProgressFooterViewHolder(view: View): RecyclerView.ViewHolder(view) {
		fun bind() {}
	}

	interface UserListAdapterListener {
		fun onSelectUserListener(user: UserEntity)
		fun invertBitmap(resource: Bitmap): Bitmap?
		fun lastPositionVisible(user: UserEntity)
	}
}
