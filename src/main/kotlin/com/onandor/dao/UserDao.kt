package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.User
import com.onandor.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class UserDao : IUserDao {

    private fun resultRowToUser(row: ResultRow) = User(
        row[Users.id],
        row[Users.email],
        row[Users.passwordHash]
    )

    override suspend fun create(email: String, passwordHash: String): Int = dbQuery {
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
        }[Users.id]
    }

    override suspend fun getByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getAll(): List<User> = dbQuery {
        Users.selectAll().map(::resultRowToUser)
    }
}

val userDao: IUserDao = UserDao()