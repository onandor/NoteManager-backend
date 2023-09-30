package com.onandor.dao

import com.onandor.models.Note
import java.util.*

class NoteService: INoteService {
    override suspend fun getAllByUser(userId: Int): List<Note> {
        return noteDao.getAllByUser(userId)
            .map { note -> note.copy(labels = labelDao.getAllIdsByUserAndNote(userId, note.id)) }
    }

    override suspend fun getById(noteId: UUID): Note? {
        val note: Note = noteDao.getById(noteId) ?: return null
        return note.copy(labels = labelDao.getAllIdsByUserAndNote(note.userId, noteId))
    }

    override suspend fun create(note: Note): UUID {
        val noteId = noteDao.create(note)
        labelDao.addAllToNote(note.id, note.labels)
        return noteId
    }

    override suspend fun update(note: Note): Int {
        val result = noteDao.update(note)
        if (result == 0)
            return result

        labelDao.addAllToNote(note.id, note.labels)
        return result
    }

    override suspend fun delete(noteId: UUID): Int {
        var result: Int = 0
        result += noteDao.delete(noteId)
        result += labelDao.removeAllFromNote(noteId)
        return result
    }
}

val noteService: INoteService = NoteService()