package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.RefreshToken
import com.onandor.models.RefreshTokens
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class RefreshTokenDao: IRefreshTokenDao {

    private fun resultRowToToken(row: ResultRow) = RefreshToken(
        row[RefreshTokens.userId],
        row[RefreshTokens.deviceId],
        row[RefreshTokens.tokenValue],
        row[RefreshTokens.expiresAt],
        row[RefreshTokens.valid]
    )

    override suspend fun getAll(): List<RefreshToken> = dbQuery {
        RefreshTokens.selectAll().map(::resultRowToToken)
    }

    override suspend fun getByTokenValue(tokenValue: UUID): RefreshToken? = dbQuery {
        RefreshTokens.select { RefreshTokens.tokenValue eq tokenValue }
            .map(::resultRowToToken)
            .singleOrNull()
    }

    override suspend fun create(token: RefreshToken): UUID = dbQuery {
        RefreshTokens.insert {
            it[userId] = token.userId
            it[deviceId] = token.deviceId
            it[tokenValue] = token.tokenValue
            it[expiresAt] = token.expiresAt
            it[valid] = token.valid
        }[RefreshTokens.tokenValue]
    }

    override suspend fun invalidate(token: RefreshToken): Unit = dbQuery {
        RefreshTokens.update( { RefreshTokens.tokenValue eq token.tokenValue } ) {
            it[valid] = false
        }
    }

    override suspend fun deleteAllByUser(userId: Int) {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
    }

    override suspend fun deleteAllByUserDevice(userId: Int, deviceId: UUID): Unit = dbQuery {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId and (RefreshTokens.deviceId eq deviceId) }
    }
}

val refreshTokenDao: IRefreshTokenDao = RefreshTokenDao()