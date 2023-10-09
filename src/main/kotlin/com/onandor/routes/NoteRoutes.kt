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
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import io.ktor.server.util.*
import java.util.UUID

@Resource("/notes")
class Notes() {
    @Resource("{id}")
    class Id(val parent: Notes = Notes(), val id: String)
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
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                call.respond(HttpStatusCode.OK, note!!)
            }
        }

        // Add new note
        authenticate {
            post<Notes> {
                val user: User = getUserFromPrincipal(call)
                val note: Note = call.receive()
                // Make sure the user doesn't create a note for somebody else
                val userNote = note.copy(
                    userId = user.id
                )
                val noteId = noteRepository.create(userNote)
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

        // Delete note
        authenticate {
            delete<Notes.Id> { noteId ->
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
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }
                noteRepository.delete(noteIdUUID)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}