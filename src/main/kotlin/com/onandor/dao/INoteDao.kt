package com.onandor.dao

import com.onandor.models.DeletedNote
import com.onandor.models.Note
import java.util.*

interface INoteDao {

    suspend fun getAllByUser(userId: Int): List<Note>

    suspend fun getById(noteId: UUID): Note?

    suspend fun getAllDeletedByUser(userId: Int): List<DeletedNote>

    suspend fun create(note: Note): UUID

    suspend fun update(note: Note): Int

    suspend fun upsertAllIfNewer(notes: List<Note>): Int

    suspend fun delete(noteId: UUID, userId: Int): Int

    suspend fun deleteAllByIds(noteIds: List<UUID>, userId: Int): Int

    suspend fun deleteAllByUser(userId: Int): Int
}