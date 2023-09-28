package com.onandor.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
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
    val userId = integer("user_id")
    val title = varchar("title", length = 2048)
    val content = varchar("content", length = 65536)
    val location = integer("location")
    val creationDate = datetime("creation_date")
    val modificationDate = datetime("modification_date")

    override val primaryKey = PrimaryKey(id)
}