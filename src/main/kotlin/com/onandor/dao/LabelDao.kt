package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.Label
import com.onandor.models.Labels
import com.onandor.models.NoteLabels
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.util.*

class LabelDao: ILabelDao {

    private fun resultRowToLabel(row: ResultRow) = Label(
        row[Labels.id],
        row[Labels.userId],
        row[Labels.title],
        row[Labels.color]
    )

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

    override suspend fun create(label: Label): UUID = dbQuery {
        Labels.insert {
            it[id] = label.id
            it[userId] = label.userId
            it[title] = label.title
            it[color] = label.color
        }[Labels.id]
    }

    override suspend fun createOrIgnore(label: Label): UUID = dbQuery {
        Labels.insertIgnore {
            it[id] = label.id
            it[userId] = label.userId
            it[title] = label.title
            it[color] = label.color
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
        }
    }

    override suspend fun updateAll(labels: List<Label>): Int = dbQuery {
        Labels.batchReplace(labels, shouldReturnGeneratedValues = true) {(id, userId, value, color) ->
            this[Labels.id] = id
            this[Labels.userId] = userId
            this[Labels.title] = value
            this[Labels.color] = color
        }.count()
    }

    override suspend fun addToNoteOrIgnore(noteId: UUID, labelId: UUID): Unit = dbQuery {
        NoteLabels.insertIgnore {
            it[NoteLabels.noteId] = noteId
            it[NoteLabels.labelId] = labelId
        }
    }

    override suspend fun addAllToNoteOrIgnore(noteId: UUID, labelIds: List<UUID>): Unit = dbQuery {
        NoteLabels.batchInsert(
            labelIds,
            ignore = true,
            shouldReturnGeneratedValues = false
        ) { labelId ->
            this[NoteLabels.noteId] = noteId
            this[NoteLabels.labelId] = labelId
        }
    }

    override suspend fun removeAllMissingFromNote(noteId: UUID, labelIds: List<UUID>): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.noteId eq noteId and (NoteLabels.labelId notInList labelIds) }
    }

    override suspend fun removeAllFromNote(noteId: UUID): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.noteId eq noteId }
    }

    override suspend fun delete(labelId: UUID): Int = dbQuery {
        var result = 0
        result += NoteLabels.deleteWhere { NoteLabels.labelId eq labelId }
        result += Labels.deleteWhere { Labels.id eq labelId }
        result
    }
}

val labelDao: ILabelDao = LabelDao()