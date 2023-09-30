package com.onandor.routes

import com.onandor.dao.noteService
import com.onandor.dao.userDao
import com.onandor.models.Note
import com.onandor.models.User
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
        suspend fun getUserFromPrincipal(call: ApplicationCall): User {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asInt()
            return userDao.getById(userId)!!
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
                val user: User = getUserFromPrincipal(call)
                val note: Note? = noteService.getById(user.id, UUID.fromString(noteId.id))
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
                val noteId = noteService.create(user.id, note)
                call.respond(HttpStatusCode.Created, noteId)
            }
        }

        // Update existing note
        authenticate {
            put<Notes> {
                val user: User = getUserFromPrincipal(call)
                val note: Note = call.receive()
                val result = noteService.update(user.id, note)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

        // Delete note
        authenticate {
            delete<Notes.Id> { noteId ->
                val user: User = getUserFromPrincipal(call)
                val result = noteService.delete(user.id, UUID.fromString(noteId.id))
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}