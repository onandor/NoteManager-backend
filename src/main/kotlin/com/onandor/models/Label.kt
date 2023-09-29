package com.onandor.models

import org.jetbrains.exposed.sql.Table
import java.util.UUID

data class Label(
    val id: UUID,
    val userId: Int,
    val value: String,
    val color: String
)

object Labels: Table() {
    val id = uuid("id")
    val userId = integer("user_id")
    val value = varchar("value", length = 30)
    val color = varchar("color", length = 7)

    override val primaryKey = PrimaryKey(id)
}

object NoteLabels: Table() {
    val labelId = uuid("label_id")
    val noteId = uuid("note_id")

    override val primaryKey = PrimaryKey(labelId, noteId)
}