package com.onandor.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class Note(
    val id: UUID,
    val userId: Int,
    val title: String,
    val content: String,
    val labels: List<Label>,
    val location: Int,
    val pinned: Boolean,
    val pinHash: String,
    val deleted: Boolean,
    val creationDate: Long,
    val modificationDate: Long
)

object Notes: Table() {
    val id = uuid("id")
    val userId = integer("user_id")
    val title = varchar("title", length = 2048)
    val content = varchar("content", length = 65536)
    val location = integer("location")
    val pinned = bool("pinned")
    val pinHash = varchar("pin_hash", length = 72)
    val deleted = bool("deleted")
    val creationDate = long("creation_date")
    val modificationDate = long("modification_date")

    override val primaryKey = PrimaryKey(id)
}