package com.onandor.dao

import com.onandor.models.DeletedNote
import com.onandor.models.Note
import java.util.*

class NoteRepository: INoteRepository {
    override suspend fun getAllByUser(userId: Int): List<Note> {
        return noteDao.getAllByUser(userId)
            .map { note -> note.copy(labels = labelDao.getAllByUserAndNote(userId, note.id)) }
    }

    override suspend fun getById(noteId: UUID): Note? {
        val note: Note = noteDao.getById(noteId) ?: return null
        return note.copy(labels = labelDao.getAllByUserAndNote(note.userId, noteId))
    }

    override suspend fun getAllDeletedByUser(userId: Int): List<DeletedNote> {
        return noteDao.getAllDeletedByUser(userId)
    }

    override suspend fun create(note: Note): UUID {
        val noteId = noteDao.create(note)
        labelDao.addAllToNoteOrIgnore(note.id, note.labels)
        return noteId
    }

    override suspend fun update(note: Note): Int {
        val result = noteDao.update(note)
        if (result == 0)
            return result

        labelDao.removeAllMissingFromNote(note.id, note.labels.map { label -> label.id })
        labelDao.addAllToNoteOrIgnore(note.id, note.labels)
        return result
    }

    override suspend fun upsertAllIfNewer(notes: List<Note>): Int {
        val result = noteDao.upsertAllIfNewer(notes)
        if (result == 0)
            return result

        notes.forEach { note ->
            labelDao.removeAllMissingFromNote(note.id, note.labels.map { label -> label.id })
            labelDao.addAllToNoteOrIgnore(note.id, note.labels)
        }
        return result
    }

    override suspend fun delete(noteId: UUID, userId: Int): Int {
        labelDao.removeAllFromNote(noteId)
        return noteDao.delete(noteId, userId)
    }

    override suspend fun deleteAllByIds(noteIds: List<UUID>, userId: Int): Int {
        labelDao.removeAllFromNotes(noteIds)
        return noteDao.deleteAllByIds(noteIds, userId)
    }

    override suspend fun deleteAllByUser(userId: Int) {
        noteDao.getAllByUser(userId).forEach { note ->
            labelDao.removeAllFromNote(note.id)
        }
        noteDao.deleteAllByUser(userId)
        labelDao.getAllByUser(userId).forEach { label ->
            labelDao.delete(label.id, userId)
        }
    }
}

val noteRepository: INoteRepository = NoteRepository()