package com.onandor.models

import org.jetbrains.exposed.sql.Table
import java.util.*

data class RefreshToken(
    val userId: Int,
    val deviceId: UUID,
    val tokenValue: UUID,
    val expiresAt: Long,
    val valid: Boolean
)

object RefreshTokens: Table() {
    val userId = integer("user_id")
    val deviceId = uuid("device_id")
    val tokenValue = uuid("token_value")
    val expiresAt = long("expires_at")
    val valid = bool("valid")

    override val primaryKey = PrimaryKey(tokenValue)
}