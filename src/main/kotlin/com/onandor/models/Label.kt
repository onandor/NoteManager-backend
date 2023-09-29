package com.onandor.models

import org.jetbrains.exposed.sql.Table

data class Label(
    val id: Int,
    val userId: Int,
    val value: String,
    val color: String
)

object Labels: Table() {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val value = varchar("value", length = 30)
    val color = varchar("color", length = 7)

    override val primaryKey = PrimaryKey(id)
}

object NoteLabels: Table() {
    val labelId = integer("label_id")
    val noteId = varchar("note_id", length = 36)

    override val primaryKey = PrimaryKey(labelId, noteId)
}