package com.onandor.dao

import com.onandor.models.Note
import java.util.*

class NoteService: INoteService {
    override suspend fun getAllByUser(userId: Int): List<Note> {
        return noteDao.getAllByUser(userId)
            .map { note -> note.copy(labels = labelDao.getAllByUserAndNote(userId, note.id)) }
    }

    override suspend fun getById(userId: Int, noteId: UUID): Note? {
        val note: Note = noteDao.getById(userId, noteId) ?: return null
        return note.copy(labels = labelDao.getAllByUserAndNote(userId, noteId))
    }

    override suspend fun create(userId: Int, note: Note): UUID {
        val noteId = noteDao.create(userId, note)
        note.labels.forEach { label ->
            labelDao.createOrIgnoreAndAddToNote(note.id, label)
        }
        return noteId
    }

    override suspend fun update(userId: Int, note: Note): Int {
        val result = noteDao.update(userId, note)
        if (result == 0)
            return result

        labelDao.addAllToNote(note.id, note.labels)
        return result
    }

    override suspend fun delete(userId: Int, noteId: UUID): Int {
        var result: Int = 0
        result += noteDao.delete(userId, noteId)
        result += labelDao.removeAllFromNote(noteId)
        return result
    }
}

val noteService: INoteService = NoteService()