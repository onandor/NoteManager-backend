package com.onandor.data

import java.time.LocalDateTime

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val location: NoteLocation,
    val creationDate: LocalDateTime,
    val modificationDate: LocalDateTime
)