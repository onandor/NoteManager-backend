package com.onandor.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

data class Note(
    val id: UUID,
    val userId: Int,
    val title: String,
    val content: String,
    val labels: List<Label>,
    val location: Int,
    val creationDate: LocalDateTime,
    val modificationDate: LocalDateTime
)

object Notes: Table() {
    val id = uuid("id")
    val userId = integer("user_id")
    val title = varchar("title", length = 2048)
    val content = varchar("content", length = 65536)
    val location = integer("location")
    val creationDate = datetime("creation_date")
    val modificationDate = datetime("modification_date")

    override val primaryKey = PrimaryKey(id)
}