package com.onandor.models

import org.jetbrains.exposed.sql.Table

data class RefreshToken(
    val userId: Int,
    val value: String,
    val expiresAt: Long,
    val valid: Boolean
)

object RefreshTokens: Table() {
    val userId = integer("user_id")
    val value = varchar("value", length = 36)
    val expiresAt = long("expires_at")
    val valid = bool("valid")

    override val primaryKey = PrimaryKey(value)
}