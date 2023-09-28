package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.RefreshToken
import com.onandor.models.RefreshTokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class RefreshTokenDao: IRefreshTokenDao {

    private fun resultRowToToken(row: ResultRow) = RefreshToken(
        row[RefreshTokens.userId],
        row[RefreshTokens.value],
        row[RefreshTokens.expiresAt],
        row[RefreshTokens.valid]
    )

    override suspend fun getAll(): List<RefreshToken> = dbQuery {
        RefreshTokens.selectAll().map(::resultRowToToken)
    }

    override suspend fun getAllByUserSortedByExpiration(userId: Int): List<RefreshToken> = dbQuery {
        RefreshTokens.select { RefreshTokens.userId eq userId }
            .map(::resultRowToToken)
            .sortedBy { it.expiresAt }
    }

    override suspend fun getByValue(value: String): RefreshToken? = dbQuery {
        RefreshTokens.select { RefreshTokens.value eq value }
            .map(::resultRowToToken)
            .singleOrNull()
    }

    override suspend fun create(token: RefreshToken): String = dbQuery {
        RefreshTokens.insert {
            it[userId] = token.userId
            it[value] = token.value
            it[expiresAt] = token.expiresAt
            it[valid] = token.valid
        }[RefreshTokens.value]
    }

    override suspend fun invalidate(token: RefreshToken): Unit = dbQuery {
        RefreshTokens.update( { RefreshTokens.value eq token.value } ) {
            it[valid] = false
        }
    }

    override suspend fun deleteAllByUser(userId: Int): Unit = dbQuery {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
    }
}

val refreshTokenDao: IRefreshTokenDao = RefreshTokenDao()