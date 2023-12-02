package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class LabelDao: ILabelDao {

    private fun resultRowToLabel(row: ResultRow) = Label(
        row[Labels.id],
        row[Labels.userId],
        row[Labels.title],
        row[Labels.color],
        row[Labels.deleted],
        row[Labels.creationDate],
        row[Labels.modificationDate]
    )

    override suspend fun getById(labelId: UUID): Label? = dbQuery {
        Labels.select { Labels.id eq labelId }
            .map(::resultRowToLabel)
            .singleOrNull()
    }

    override suspend fun getAllByUser(userId: Int): List<Label> = dbQuery {
        Labels.select { Labels.userId eq userId }
            .map(::resultRowToLabel)
    }

    override suspend fun getAllByUserAndNote(userId: Int, noteId: UUID): List<Label> = dbQuery {
        val join = Join(
            Labels, NoteLabels,
            onColumn = Labels.id,
            otherColumn = NoteLabels.labelId,
            joinType = JoinType.INNER,
            additionalConstraint = { Labels.userId eq userId and (NoteLabels.noteId eq noteId) }
        )
        join.selectAll().map(::resultRowToLabel)
    }

    override suspend fun getAllIdsByUserAndNote(userId: Int, noteId: UUID): List<UUID> = dbQuery {
        getAllByUserAndNote(userId, noteId).map { label -> label.id }
    }

    override suspend fun getAllDeletedByUser(userId: Int): List<DeletedLabel> = dbQuery {
        DeletedLabels.select { DeletedLabels.userId eq userId }
            .map { row ->
                DeletedLabel(
                    labelId = row[DeletedLabels.labelId],
                    userId = row[DeletedLabels.userId]
                )
            }
    }

    override suspend fun create(label: Label): UUID = dbQuery {
        Labels.insert {
            it[id] = label.id
            it[userId] = label.userId
            it[title] = label.title
            it[color] = label.color
            it[deleted] = label.deleted
            it[creationDate] = label.creationDate
            it[modificationDate] = label.modificationDate
        }[Labels.id]
    }

    override suspend fun createOrIgnore(label: Label): UUID = dbQuery {
        Labels.insertIgnore {
            it[id] = label.id
            it[userId] = label.userId
            it[title] = label.title
            it[color] = label.color
            it[deleted] = label.deleted
            it[creationDate] = creationDate
            it[modificationDate] = modificationDate
        }[Labels.id]
    }

    override suspend fun createOrIgnoreAndAddToNote(noteId: UUID, label: Label): UUID {
        val labelId = createOrIgnore(label)
        NoteLabels.insert {
            it[NoteLabels.labelId] = labelId
            it[NoteLabels.noteId] = noteId
        }
        return labelId
    }

    override suspend fun update(label: Label): Int = dbQuery {
        Labels.update( { Labels.id eq label.id } ) {
            it[title] = label.title
            it[color] = label.color
            it[modificationDate] = label.modificationDate
        }
    }

    override suspend fun updateAll(labels: List<Label>): Int = dbQuery {
        Labels.batchReplace(
            labels, shouldReturnGeneratedValues = true
        ) {(id, userId, value, color, deleted, creationDate, modificationDate) ->
            this[Labels.id] = id
            this[Labels.userId] = userId
            this[Labels.title] = value
            this[Labels.color] = color
            this[Labels.deleted] = deleted
            this[Labels.creationDate] = creationDate
            this[Labels.modificationDate] = modificationDate
        }.count()
    }

    override suspend fun addToNoteOrIgnore(noteId: UUID, label: Label): Unit = dbQuery {
        NoteLabels.insertIgnore {
            it[NoteLabels.noteId] = noteId
            it[NoteLabels.labelId] = label.id
        }
    }

    override suspend fun addAllToNoteOrIgnore(noteId: UUID, labels: List<Label>): Unit = dbQuery {
        NoteLabels.batchInsert(
            labels,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { label ->
            this[NoteLabels.noteId] = noteId
            this[NoteLabels.labelId] = label.id
        }
    }

    override suspend fun removeAllMissingFromNote(noteId: UUID, labelIds: List<UUID>): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.noteId eq noteId and (NoteLabels.labelId notInList labelIds) }
    }

    override suspend fun removeAllFromNote(noteId: UUID): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.noteId eq noteId }
    }

    override suspend fun removeAllFromNotes(noteIds: List<UUID>): Int = dbQuery {
        var deleted = 0
        noteIds.forEach { noteId ->
            deleted += NoteLabels.deleteWhere { NoteLabels.noteId eq noteId }
        }
        deleted
    }

    override suspend fun delete(labelId: UUID, userId: Int): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.labelId eq labelId }
        DeletedLabels.insertIgnore {
            it[DeletedLabels.labelId] = labelId
            it[DeletedLabels.userId] = userId
        }
        Labels.deleteWhere { Labels.id eq labelId }
    }

    override suspend fun deleteAllByIds(labelIds: List<UUID>, userId: Int): Int = dbQuery {
        DeletedLabels.batchInsert(
            data = labelIds,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { labelId ->
            this[DeletedLabels.labelId] = labelId
            this[DeletedLabels.userId] = userId
        }
        var result = 0
        labelIds.forEach { labelId ->
            NoteLabels.deleteWhere { NoteLabels.labelId eq labelId }
            result += Labels.deleteWhere { Labels.id eq labelId }
        }
        result
    }

    override suspend fun upsertAllIfNewer(labels: List<Label>) = dbQuery {
        Labels.batchInsert(
            data = labels,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { label ->
            this[Labels.id] = label.id
            this[Labels.userId] = label.userId
            this[Labels.title] = label.title
            this[Labels.color] = label.color
            this[Labels.deleted] = label.deleted
            this[Labels.creationDate] = label.creationDate
            this[Labels.modificationDate] = label.modificationDate
        }
        labels.forEach { label ->
            Labels.update({ Labels.id eq label.id and (Labels.modificationDate less label.modificationDate) }) {
                it[title] = label.title
                it[color] = label.color
                it[modificationDate] = label.modificationDate
            }
        }
    }
}

val labelDao: ILabelDao = LabelDao()