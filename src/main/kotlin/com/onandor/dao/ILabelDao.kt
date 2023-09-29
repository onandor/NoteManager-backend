package com.onandor.dao

import com.onandor.models.Label

interface ILabelDao {

    suspend fun getAllByUser(userId: Int): List<Label>

    suspend fun getAllByUserAndNote(userId: Int, noteId: String): List<Label>

    suspend fun create(label: Label): Int

    suspend fun createAndAddToNote(noteId: String, label: Label): Int

    suspend fun deleteFromNote(noteId: String, labelId: Int): Int

    suspend fun delete(labelId: Int): Int
}