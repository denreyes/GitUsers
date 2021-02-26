package com.denreyes.githubusers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.denreyes.githubusers.repository.local.RoomAppDb
import com.denreyes.githubusers.repository.local.UserDao
import com.denreyes.githubusers.repository.local.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.IsEqual.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserDaoTest {
	private lateinit var database: RoomAppDb
	private lateinit var dao: UserDao

	@Before
	fun setup() {
		val context = ApplicationProvider.getApplicationContext<Context>()
		database = Room.inMemoryDatabaseBuilder(
			context, RoomAppDb::class.java
		)
			.allowMainThreadQueries()
			.build()
		dao = database.userDao()!!
	}

	@After
	@Throws(IOException::class)
	fun tearDown() {
		database.close()
	}

	@Test
	fun insertUser() = runBlockingTest {
		val user = UserEntity(
			1, "login1", "url1", "user",
			"den", "company1", "blog1", 0, 0, ""
		)
		dao.insertUser(user)

		val u = dao.getUser(1)
		assertThat(u, equalTo(user))
	}

	@Test
	fun deleteUser() = runBlockingTest {
		val user = UserEntity(
			1, "login1", "url1", "user",
			"den", "company1", "blog1", 0, 0, ""
		)
		dao.insertUser(user)
		dao.deleteUser(user)

		val users = dao.getAllUsers()
		assertThat(users, not(CoreMatchers.hasItem(user)))
	}

	@Test
	fun updateUser() = runBlockingTest {
		val user1 = UserEntity(
			1, "login1", "url1", "user",
			"den", "company1", "blog1", 0, 0,
			""
		)
		dao.insertUser(user1)
		val u1 = dao.getUser(1)!!
		assertEquals(u1.note, "")

		val user2 = UserEntity(
			1, "login1", "url1", "user",
			"den", "company1", "blog1", 0, 0,
			"This is a note"
		)
		dao.updateUser(user2)

		val u2 = dao.getUser(1)!!
		assertEquals(u2.note, "This is a note")
	}

}
