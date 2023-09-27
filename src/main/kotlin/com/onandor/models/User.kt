package com.onandor.models

import org.jetbrains.exposed.sql.Table

data class User(
    val id: Int,
    val email: String,
    val password: String
)

object Users: Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", length = 255)
    val passwordHash = varchar("password_hash", length = 72)

    override val primaryKey = PrimaryKey(id)
}