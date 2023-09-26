package com.onandor.data

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class NoteService(private val database: Database) {
    object Notes: Table() {
        val id = varchar("id", length = 36)
        val userId = varchar("user_id", length = 36)
        val title = varchar("title", length = 2048)
        val content = varchar("title", length = Int.MAX_VALUE)
        val location = integer("location")
        val creationDate = datetime("creation_date")
        val modificationDate = datetime("modification_date")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Notes)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(note: Note): String = dbQuery {
        Notes.insert {
            it[id] = note.id
            it[userId] = note.userId
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location.value
            it[creationDate] = note.creationDate
            it[modificationDate] = note.modificationDate
        }[Notes.id]
    }

    suspend fun getById(id: String): Note? = dbQuery {
        Notes.select { Notes.id eq id }
            .map {
                Note(
                    it[Notes.id],
                    it[Notes.userId],
                    it[Notes.title],
                    it[Notes.content],
                    NoteLocation.fromInt(it[Notes.location]),
                    it[Notes.creationDate],
                    it[Notes.modificationDate]
                )
            }
            .singleOrNull()
    }

    suspend fun update(id: String, note: Note) = dbQuery {
        Notes.update( { Notes.id eq id } ) {
            it[Notes.id] = note.id
            it[userId] = note.userId
            it[title] = note.title
            it[content] = note.content
            it[location] = note.location.value
            it[creationDate] = note.creationDate
            it[modificationDate] = note.modificationDate
        }
    }

    suspend fun delete(id: String) = dbQuery {
        Notes.deleteWhere { Notes.id eq id }
    }
}