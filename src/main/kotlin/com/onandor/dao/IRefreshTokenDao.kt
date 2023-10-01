package com.onandor.dao

import com.onandor.models.RefreshToken
import java.util.*

interface IRefreshTokenDao {

    suspend fun getAll(): List<RefreshToken>

    suspend fun getByTokenValue(tokenValue: UUID): RefreshToken?

    suspend fun create(token: RefreshToken): UUID

    suspend fun invalidate(token: RefreshToken)

    suspend fun deleteAllByUser(userId: Int)

    suspend fun deleteAllByUserDevice(userId: Int, deviceId: UUID)
}