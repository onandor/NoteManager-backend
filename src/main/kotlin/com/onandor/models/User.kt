package com.onandor.models

import org.jetbrains.exposed.sql.Table
import java.util.*

data class User(
    val id: Int = -1,
    val email: String,
    val password: String,
    val deviceId: UUID?
)

object Users: Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", length = 255)
    val passwordHash = varchar("password_hash", length = 72)

    override val primaryKey = PrimaryKey(id)
}