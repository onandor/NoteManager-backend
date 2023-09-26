package com.onandor.dao

import com.onandor.models.Note

interface INoteDao {

    suspend fun getAllByUser(userId: String): List<Note>

    suspend fun getById(userId: String, noteId: String): Note?

    suspend fun create(userId: String, note: Note): String

    suspend fun update(userId: String, note: Note)

    suspend fun delete(userId: String, noteId: String)
}