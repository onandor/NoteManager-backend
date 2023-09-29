package com.onandor.dao

import com.onandor.dao.DatabaseFactory.dbQuery
import com.onandor.models.Label
import com.onandor.models.Labels
import com.onandor.models.NoteLabels
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class LabelDao: ILabelDao {

    private fun resultRowToLabel(row: ResultRow) = Label(
        row[Labels.id],
        row[Labels.userId],
        row[Labels.value],
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

    override suspend fun create(label: Label): UUID = dbQuery {
        Labels.insert {
            it[id] = label.id
            it[userId] = label.userId
            it[value] = label.value
            it[color] = label.color
        }[Labels.id]
    }

    override suspend fun createAndAddToNote(noteId: UUID, label: Label): UUID {
        val labelId = create(label)
        NoteLabels.insert {
            it[NoteLabels.labelId] = labelId
            it[NoteLabels.noteId] = noteId
        }
        return labelId
    }

    override suspend fun deleteFromNote(noteId: UUID, labelId: UUID): Int = dbQuery {
        NoteLabels.deleteWhere { NoteLabels.noteId eq noteId and (NoteLabels.labelId eq labelId) }
    }

    override suspend fun delete(labelId: UUID): Int = dbQuery {
        var result = 0
        result += NoteLabels.deleteWhere { NoteLabels.labelId eq labelId }
        result += Labels.deleteWhere { Labels.id eq labelId }
        result
    }
}

val labelDao: ILabelDao = LabelDao()