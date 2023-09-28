package com.onandor.dao

import com.onandor.models.RefreshToken

interface IRefreshTokenDao {

    suspend fun getAll(): List<RefreshToken>

    suspend fun getAllByUserSortedByExpiration(userId: Int): List<RefreshToken>

    suspend fun getByValue(value: String): RefreshToken?

    suspend fun create(token: RefreshToken): String

    suspend fun invalidate(token: RefreshToken)

    suspend fun deleteAllByUser(userId: Int)
}