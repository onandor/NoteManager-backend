package com.onandor.routes

import com.onandor.dao.noteRepository
import com.onandor.models.Note
import com.onandor.models.User
import com.onandor.plugins.getUserFromPrincipal
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.util.UUID

@Resource("/notes")
class Notes() {
    @Resource("{id}")
    class Id(val parent: Notes = Notes(), val id: String)

    @Resource("delete")
    class Delete(val parent: Notes = Notes()) {

        @Resource("{id}")
        class Id(val parent: Delete = Delete(), val id: String)
    }

    @Resource("sync")
    class Sync(val parent: Notes = Notes()) {

        @Resource("single")
        class Single(val parent: Sync = Sync())
    }
}

fun Application.configureNoteRoutes() {
    routing {
        suspend fun checkIsUserOwner(userId: Int, noteId: UUID): Boolean {
            return noteRepository.getAllByUser(userId)
                .any { note -> note.id == noteId && note.userId == userId }
        }

        // Get all notes of a user
        authenticate {
            get<Notes> {
                val user: User = getUserFromPrincipal(call)
                val notes: List<Note> = noteRepository.getAllByUser(user.id)
                call.respond(HttpStatusCode.OK, notes)
            }
        }

        // Get a specific note of a user by note id
        authenticate {
            get<Notes.Id> {noteId ->
                val noteIdUUID: UUID
                try {
                    noteIdUUID = UUID.fromString(noteId.id)
                } catch (e: java.lang.IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val note: Note? = noteRepository.getById(noteIdUUID)
                if (note == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val user: User = getUserFromPrincipal(call)
                if (!checkIsUserOwner(user.id, noteIdUUID)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }
                call.respond(HttpStatusCode.OK, note)
            }
        }

        // Add new note
        authenticate {
            post<Notes> {
                val user: User = getUserFromPrincipal(call)
                val note: Note = call.receive<Note>().copy(userId = user.id)
                val noteId = noteRepository.create(note)
                call.respond(HttpStatusCode.Created, hashMapOf("id" to noteId))
            }
        }

        // Update existing note
        authenticate {
            put<Notes> {
                val note: Note = call.receive()
                if (noteRepository.getById(note.id) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                val user: User = getUserFromPrincipal(call)
                if (!checkIsUserOwner(user.id, note.id)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }
                noteRepository.update(note)
                call.respond(HttpStatusCode.OK)
            }
        }

        // Update or insert modified or new notes, delete missing notes
        authenticate {
            put<Notes.Sync> {
                val user: User = getUserFromPrincipal(call)
                val notes: List<Note> = call.receive()
                val userNotes = noteRepository.getAllByUser(user.id)
                val ownedNotes = notes.filter { note ->
                    userNotes.any { userNote -> note.id == userNote.id && note.userId == userNote.userId }
                }
                val partitionedNotes = ownedNotes.partition { note -> !note.deleted }
                // Delete all notes that were marked as deleted on the client since the last sync
                noteRepository.deleteAllByIds(partitionedNotes.second.map { note -> note.id }, user.id)
                val deletedNotes = noteRepository.getAllDeletedByUser(user.id)
                // Only upsert notes that aren't marked as deleted (maybe by another client)
                val notesToUpsert = partitionedNotes.first.filterNot { note ->
                    deletedNotes.any { deletedNote -> note.id == deletedNote.noteId }
                }
                noteRepository.upsertAllIfNewer(notesToUpsert)
                call.respond(HttpStatusCode.OK)
            }
        }

        authenticate {
            put<Notes.Sync.Single> {
                val user: User = getUserFromPrincipal(call)
                val note: Note = call.receive<Note>().copy(userId = user.id)
                if (note.deleted) {
                    noteRepository.delete(note.id, user.id)
                    call.respond(HttpStatusCode.OK)
                    return@put
                }
                val deletedNotes = noteRepository.getAllDeletedByUser(user.id)
                if (!deletedNotes.any { deletedNote -> note.id == deletedNote.noteId }) {
                    noteRepository.upsertAllIfNewer(listOf(note))
                }
                call.respond(HttpStatusCode.OK)
            }
        }

        // Delete note
        authenticate {
            delete<Notes.Delete.Id> { noteId ->
                val noteIdUUID: UUID
                try {
                    noteIdUUID = UUID.fromString(noteId.id)
                } catch (e: java.lang.IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                if (noteRepository.getById(noteIdUUID) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                val user: User = getUserFromPrincipal(call)
                if (!checkIsUserOwner(user.id, noteIdUUID)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete
                }
                noteRepository.delete(noteIdUUID, user.id)
                call.respond(HttpStatusCode.OK)
            }
        }

        // Delete multiple notes at the same time
        authenticate {
            post<Notes.Delete> {
                val noteIdsString: List<String> = call.receive()
                val user: User = getUserFromPrincipal(call)
                val userNotes = noteRepository.getAllByUser(user.id)
                val ownedNoteIds: MutableList<UUID> = mutableListOf()
                noteIdsString.forEach { noteIdString ->
                    val noteId = try {
                        UUID.fromString(noteIdString)
                    } catch (e: java.lang.IllegalArgumentException) {
                        return@forEach
                    }
                    if (userNotes.any { userNote -> noteId == userNote.id }) {
                        ownedNoteIds.add(noteId)
                    }
                }
                noteRepository.deleteAllByIds(ownedNoteIds, user.id)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}