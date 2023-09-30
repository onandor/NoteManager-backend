package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.Note
import com.onandor.models.Notes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class NoteDao : INoteDao {

    private fun resultRowToNote(row: ResultRow) = Note(
        row[Notes.id],
        row[Notes.userId],
        row[Notes.title],
        row[Notes.content],
        emptyList(),
        row[Notes.location],
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

    override suspend fun create(note: Note) = dbQuery {
        Notes.insert {
            it[id] = note.id
            it[userId] = note.userId
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[creationDate] = note.creationDate
            it[modificationDate] = note.modificationDate
        }[Notes.id]
    }

    override suspend fun update(note: Note): Int = dbQuery {
        Notes.update( { Notes.id eq note.id } ) {
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location
            it[modificationDate] = note.modificationDate
        }
    }

    override suspend fun delete(noteId: UUID): Int = dbQuery {
        Notes.deleteWhere { Notes.id eq noteId }
    }
}

val noteDao: INoteDao = NoteDao()