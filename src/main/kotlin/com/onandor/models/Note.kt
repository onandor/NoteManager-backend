package com.onandor.models

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val location: Int,
    val creationDate: LocalDateTime,
    val modificationDate: LocalDateTime
)

object Notes: Table() {
    val id = varchar("id", length = 36)
    val userId = varchar("user_id", length = 36)
    val title = varchar("title", length = 2048)
    val content = varchar("content", length = 65536)
    val location = integer("location")
    val creationDate = datetime("creation_date")
    val modificationDate = datetime("modification_date")

    override val primaryKey = PrimaryKey(id)
}