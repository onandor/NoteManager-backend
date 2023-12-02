package com.onandor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID

data class Label(
    val id: UUID,
    val userId: Int,
    val title: String,
    val color: Int,
    val deleted: Boolean,
    val creationDate: Long,
    val modificationDate: Long
)

data class DeletedLabel(
    val labelId: UUID,
    val userId: Int
)

object Labels: Table() {
    val id = uuid("id")
    val userId = integer("user_id")
    val title = varchar("title", length = 30)
    val color = integer("color")
    val deleted = bool("deleted")
    val creationDate = long("creation_date")
    val modificationDate = long("modification_date")

    override val primaryKey = PrimaryKey(id)
}

object NoteLabels: Table() {
    val labelId = uuid("label_id")
    val noteId = uuid("note_id")

    override val primaryKey = PrimaryKey(labelId, noteId)
}

object DeletedLabels: Table() {
    val labelId = uuid("label_id")
    val userId = integer("user_id")

    override val primaryKey = PrimaryKey(labelId)
}