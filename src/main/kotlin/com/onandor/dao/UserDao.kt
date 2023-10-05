package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.User
import com.onandor.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserDao : IUserDao {

    private fun resultRowToUser(row: ResultRow) = User(
        row[Users.id],
        row[Users.email],
        row[Users.passwordHash],
        null
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

    override suspend fun getById(userId: Int): User? = dbQuery {
        Users.select { Users.id eq userId }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    override suspend fun getAll(): List<User> = dbQuery {
        Users.selectAll().map(::resultRowToUser)
    }

    override suspend fun delete(userId: Int): Int = dbQuery {
        Users.deleteWhere { Users.id eq userId }
    }

    override suspend fun updatePassword(userId: Int, passwordHash: String): Unit = dbQuery {
        Users.update({ Users.id eq userId }) {
            it[Users.passwordHash] = passwordHash
        }
    }
}

val userDao: IUserDao = UserDao()