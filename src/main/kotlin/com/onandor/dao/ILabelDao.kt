package com.onandor.dao

import com.onandor.models.Label
import java.util.*

interface ILabelDao {

    suspend fun getAllByUser(userId: Int): List<Label>

    suspend fun getAllByUserAndNote(userId: Int, noteId: UUID): List<Label>

    suspend fun create(label: Label): UUID

    suspend fun createAndAddToNote(noteId: UUID, label: Label): UUID

    suspend fun deleteFromNote(noteId: UUID, labelId: UUID): Int

    suspend fun delete(labelId: UUID): Int
}