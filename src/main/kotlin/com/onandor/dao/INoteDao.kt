package com.onandor.dao

import com.onandor.models.Note
import java.util.*

interface INoteDao {

    suspend fun getAllByUser(userId: Int): List<Note>

    suspend fun getById(userId: Int, noteId: UUID): Note?

    suspend fun create(userId: Int, note: Note): UUID

    suspend fun update(userId: Int, note: Note): Int

    suspend fun delete(userId: Int, noteId: UUID): Int
}