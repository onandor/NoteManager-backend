package com.onandor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID

data class Label(
    val id: UUID,
    val userId: Int,
    val title: String,
    val color: Int
)

object Labels: Table() {
    val id = uuid("id")
    val userId = integer("user_id")
    val title = varchar("title", length = 30)
    val color = integer("color")

    override val primaryKey = PrimaryKey(id)
}

object NoteLabels: Table() {
    val labelId = uuid("label_id")
    val noteId = uuid("note_id")

    override val primaryKey = PrimaryKey(labelId, noteId)
}