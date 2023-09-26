package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.Note
import com.onandor.models.Notes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class NoteDao : INoteDao {

    private fun resultRowToNote(row: ResultRow) = Note(
        row[Notes.id],
        row[Notes.title],
        row[Notes.content],
        row[Notes.location],
        row[Notes.creationDate],
        row[Notes.modificationDate]
    )

    override suspend fun getAllByUser(userId: String): List<Note> = dbQuery {
        Notes.select { Notes.userId eq userId }
            .map(::resultRowToNote)
    }

    override suspend fun getById(userId: String, noteId: String): Note? = dbQuery {
        Notes.select { Notes.id eq noteId and (Notes.userId eq userId) }
            .map(::resultRowToNote)
            .singleOrNull()
    }

    override suspend fun create(userId: String, note: Note) = dbQuery {
        Notes.insert {
            it[id] = note.id
            it[Notes.userId] = userId
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[creationDate] = note.creationDate
            it[modificationDate] = note.modificationDate
        }[Notes.id]
    }

    override suspend fun update(userId: String, note: Note): Unit = dbQuery {
        Notes.update( { Notes.userId eq userId and (Notes.id eq note.id) } ) {
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[modificationDate] = note.modificationDate
        }
    }

    override suspend fun delete(userId: String, noteId: String): Unit = dbQuery {
        Notes.deleteWhere { Notes.userId eq userId and (Notes.id eq noteId) }
    }
}

val noteDao: INoteDao = NoteDao()