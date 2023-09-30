package com.onandor.routes

import com.onandor.dao.noteService
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
            return noteService.getAllByUser(userId)
                .any { note -> note.id == noteId && note.userId == userId }
        }

        // Get all notes of a user
        authenticate {
            get<Notes> {
                val user: User = getUserFromPrincipal(call)
                val notes: List<Note> = noteService.getAllByUser(user.id)
                call.respond(HttpStatusCode.OK, notes)
            }
        }

        // Get a specific note of a user by note id
        authenticate {
            get<Notes.Id> {noteId ->
                val noteIdUUID = UUID.fromString(noteId.id)
                val user: User = getUserFromPrincipal(call)
                if (!checkIsUserOwner(user.id, noteIdUUID)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val note: Note? = noteService.getById(noteIdUUID)
                if (note == null)
                    call.respond(HttpStatusCode.NotFound)
                else
                    call.respond(HttpStatusCode.OK, note)
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
                val noteId = noteService.create(userNote)
                call.respond(HttpStatusCode.Created, noteId)
            }
        }

        // Update existing note
        authenticate {
            put<Notes> {
                val user: User = getUserFromPrincipal(call)
                val note: Note = call.receive()
                if (!checkIsUserOwner(user.id, note.id)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                val result = noteService.update(note)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

        // Delete note
        authenticate {
            delete<Notes.Id> { noteId ->
                val noteIdUUID = UUID.fromString(noteId.id)
                val user: User = getUserFromPrincipal(call)
                if (!checkIsUserOwner(user.id, noteIdUUID)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val result = noteService.delete(noteIdUUID)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}