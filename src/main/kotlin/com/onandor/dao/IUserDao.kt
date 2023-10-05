package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.User
import com.onandor.models.Users
import org.jetbrains.exposed.sql.select

interface IUserDao {

    suspend fun create(email: String, passwordHash: String): Int

    suspend fun getByEmail(email: String): User?

    suspend fun getById(userId: Int): User?

    suspend fun getAll(): List<User>

    suspend fun delete(userId: Int): Int

    suspend fun updatePassword(userId: Int, passwordHash: String)
}