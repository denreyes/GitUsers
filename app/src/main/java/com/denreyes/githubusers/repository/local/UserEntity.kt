package com.denreyes.githubusers.repository.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "userinfo")
data class UserEntity (

	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "id")
	val id : Int = 0,

	@ColumnInfo(name = "login")
	val login: String,

	@ColumnInfo(name = "avatar_url")
	val avatarUrl: String,

	@ColumnInfo(name = "type")
	val type: String,

	@ColumnInfo(name = "name")
	val name: String,

	@ColumnInfo(name = "company")
	val company: String,

	@ColumnInfo(name = "blog")
	val blog: String,

	@ColumnInfo(name = "followers")
	val followers: Int,

	@ColumnInfo(name = "following")
	val following: Int,

	@ColumnInfo(name = "note")
	val note: String
)
