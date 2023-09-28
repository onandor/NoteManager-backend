package com.onandor.dao

import com.onandor.models.Note

interface INoteDao {

    suspend fun getAllByUser(userId: Int): List<Note>

    suspend fun getById(userId: Int, noteId: String): Note?

    suspend fun create(userId: Int, note: Note): String

    suspend fun update(userId: Int, note: Note): Int

    suspend fun delete(userId: Int, noteId: String): Int
}