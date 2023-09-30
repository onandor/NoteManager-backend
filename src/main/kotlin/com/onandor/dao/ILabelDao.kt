package com.onandor.dao

import com.onandor.models.Label
import java.util.*

interface ILabelDao {

    suspend fun getAllByUser(userId: Int): List<Label>

    suspend fun getAllByUserAndNote(userId: Int, noteId: UUID): List<Label>

    suspend fun createOrIgnore(label: Label): UUID

    suspend fun update(label: Label): Int

    suspend fun updateAll(labels: List<Label>): Int

    suspend fun createOrIgnoreAndAddToNote(noteId: UUID, label: Label): UUID

    suspend fun addAllToNote(noteId: UUID, labels: List<Label>)

    suspend fun removeFromNote(noteId: UUID, labelId: UUID): Int

    suspend fun removeAllFromNote(noteId: UUID): Int

    suspend fun delete(labelId: UUID): Int
}