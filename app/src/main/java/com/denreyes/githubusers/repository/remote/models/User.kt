package com.denreyes.githubusers.repository.remote.models

import com.google.gson.annotations.SerializedName

class User {
	@SerializedName("id")
	var id: Int = 0

	@SerializedName("login")
	var login: String = ""

	@SerializedName("avatar_url")
	var avatarUrl: String = ""

	@SerializedName("type")
	var type: String = ""

	@SerializedName("name")
	var name: String = ""

	@SerializedName("company")
	var company: String = ""

	@SerializedName("blog")
	var blog: String = ""

	@SerializedName("followers")
	var followers: Int = 0

	@SerializedName("following")
	var following: Int = 0

	constructor() { }
	constructor(
		id: Int,
		login: String,
		avatarUrl: String,
		type: String,
		name: String,
		company: String,
		blog: String,
		followers: Int,
		following: Int
	) {
		this.id = id
		this.login = login
		this.avatarUrl = avatarUrl
		this.type = type
		this.name = name
		this.company = company
		this.blog = blog
		this.followers = followers
		this.following = following
	}


}
