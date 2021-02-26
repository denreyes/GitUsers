package com.denreyes.githubusers.repository.local

import androidx.room.*

@Dao
interface UserDao {

	@Query("SELECT * FROM userinfo ORDER BY id ASC")
	fun getAllUsers(): List<UserEntity>?

	@Query("SELECT * FROM userinfo WHERE id = :id")
	fun getUser(id: Int?): UserEntity?

	@Query("SELECT * FROM userinfo WHERE login LIKE :query")
	fun searchUsers(query: String): List<UserEntity>?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertUser(user: UserEntity?)

	@Delete
	fun deleteUser(user: UserEntity?)

	@Update
	fun updateUser(user: UserEntity?)
}
