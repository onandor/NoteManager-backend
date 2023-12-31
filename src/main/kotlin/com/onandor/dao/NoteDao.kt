package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.DeletedNote
import com.onandor.models.DeletedNotes
import com.onandor.models.Note
import com.onandor.models.Notes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class NoteDao : INoteDao {

    private fun resultRowToNote(row: ResultRow) = Note(
        row[Notes.id],
        row[Notes.userId],
        row[Notes.title],
        row[Notes.content],
        emptyList(),
        row[Notes.location],
        row[Notes.pinned],
        row[Notes.pinHash],
        row[Notes.deleted],
        row[Notes.creationDate],
        row[Notes.modificationDate]
    )

    override suspend fun getAllByUser(userId: Int): List<Note> = dbQuery {
        Notes.select { Notes.userId eq userId }
            .map(::resultRowToNote)
    }

    override suspend fun getById(noteId: UUID): Note? = dbQuery {
        Notes.select { Notes.id eq noteId }
            .map(::resultRowToNote)
            .singleOrNull()
    }

    override suspend fun getAllDeletedByUser(userId: Int): List<DeletedNote> = dbQuery {
        DeletedNotes.select { DeletedNotes.userId eq userId }
            .map { row ->
                DeletedNote(
                    noteId = row[DeletedNotes.noteId],
                    userId = row[DeletedNotes.userId]
                )
            }
    }

    override suspend fun create(note: Note) = dbQuery {
        Notes.insert {
            it[id] = note.id
            it[userId] = note.userId
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[pinned] = note.pinned
            it[pinHash] = note.pinHash
            it[deleted] = note.deleted
            it[creationDate] = note.creationDate
            it[modificationDate] = note.modificationDate
        }[Notes.id]
    }

    override suspend fun update(note: Note): Int = dbQuery {
        Notes.update( { Notes.id eq note.id } ) {
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[pinned] = note.pinned
            it[pinHash] = note.pinHash
            it[deleted] = note.deleted
            it[modificationDate] = note.modificationDate
        }
    }

    override suspend fun upsertAllIfNewer(notes: List<Note>): Int = dbQuery {
        Notes.batchInsert(
            data = notes,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { note ->
            this[Notes.id] = note.id
            this[Notes.userId] = note.userId
            this[Notes.title] = note.title
            this[Notes.content] = note.content
            this[Notes.location] = note.location
            this[Notes.pinned] = note.pinned
            this[Notes.pinHash] = note.pinHash
            this[Notes.deleted] = note.deleted
            this[Notes.creationDate] = note.creationDate
            this[Notes.modificationDate] = note.modificationDate
        }
        notes.forEach { note ->
            Notes.update(
                { Notes.id eq note.id and (Notes.modificationDate less note.modificationDate) }
            ) {
                it[title] = note.title
                it[content] = note.content
                it[location] = note.location
                it[pinned] = note.pinned
                it[pinHash] = note.pinHash
                it[deleted] = note.deleted
                it[modificationDate] = note.modificationDate
            }
        }
        // batchInsert always returns with ALL the rows it checked and not just with the rows it ACTUALLY inserted,
        // so the number of affected rows cannot be correctly determined
        // batchUpsert doesn't work with h2 MYSQL compatibility mode, so I cannot use that for now
        // Here is this random number, I don't care
        122114
    }

    override suspend fun delete(noteId: UUID, userId: Int): Int = dbQuery {
        DeletedNotes.insertIgnore {
            it[DeletedNotes.noteId] = noteId
            it[DeletedNotes.userId] = userId
        }
        Notes.deleteWhere { Notes.id eq noteId }
    }

    override suspend fun deleteAllByIds(noteIds: List<UUID>, userId: Int): Int = dbQuery {
        DeletedNotes.batchInsert(
            data = noteIds,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) {  noteId ->
            this[DeletedNotes.noteId] = noteId
            this[DeletedNotes.userId] = userId
        }
        var deleted = 0
        noteIds.forEach { noteId ->
            deleted += Notes.deleteWhere { Notes.id eq noteId }
        }
        deleted
    }

    override suspend fun deleteAllByUser(userId: Int): Int = dbQuery {
        val noteIds = Notes
            .select { Notes.userId eq userId }
            .map { row -> row[Notes.id] }
        DeletedNotes.batchInsert(
            data = noteIds,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { noteId ->
            this[DeletedNotes.noteId] = noteId
            this[DeletedNotes.userId] = userId
        }
        Notes.deleteWhere { Notes.userId eq userId }
    }
}

val noteDao: INoteDao = NoteDao()