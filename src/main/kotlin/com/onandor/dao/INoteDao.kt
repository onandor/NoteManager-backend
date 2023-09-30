package com.onandor.dao

import com.onandor.models.Note
import java.util.*

interface INoteDao {

    suspend fun getAllByUser(userId: Int): List<Note>

    suspend fun getById(noteId: UUID): Note?

    suspend fun create(note: Note): UUID

    suspend fun update(note: Note): Int

    suspend fun delete(noteId: UUID): Int
}