package com.onandor.dao

import com.onandor.models.Note
import java.util.UUID

interface INoteRepository {

    suspend fun getAllByUser(userId: Int): List<Note>

    suspend fun getById(noteId: UUID): Note?

    suspend fun create(note: Note): UUID

    suspend fun update(note: Note): Int

    suspend fun upsertAllIfNewer(notes: List<Note>): Int

    suspend fun delete(noteId: UUID): Int

    suspend fun deleteAllByIds(noteIds: List<UUID>): Int

    suspend fun deleteAllByUser(userId: Int)
}