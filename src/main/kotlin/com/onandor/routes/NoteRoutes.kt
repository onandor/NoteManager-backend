package com.onandor.routes

import com.onandor.dao.noteDao
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

@Resource("/notes")
class Notes() {
    @Resource("{id}")
    class Id(val parent: Notes = Notes(), val id: String)
}

fun Application.configureNoteRoutes() {
    routing {
        suspend fun getUserFromPrincipal(call: ApplicationCall): User? {
            val principal = call.principal<JWTPrincipal>()
            val email = principal!!.payload.getClaim("email").asString()
            return userDao.getByEmail(email)
        }

        // Get all notes of a user
        authenticate {
            get<Notes> {
                val user: User? = getUserFromPrincipal(call)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val notes: List<Note> = noteDao.getAllByUser(user.id)
                call.respond(HttpStatusCode.OK, notes)
            }
        }

        // Get a specific note of a user by note id
        authenticate {
            get<Notes.Id> {noteId ->
                val user: User? = getUserFromPrincipal(call)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                val note: Note? = noteDao.getById(user.id, noteId.id)
                if (note == null)
                    call.respond(HttpStatusCode.NotFound)
                else
                    call.respond(HttpStatusCode.OK, note)
            }
        }

        // Add new note
        authenticate {
            post<Notes> {
                val user: User? = getUserFromPrincipal(call)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@post
                }

                val note: Note = call.receive()
                val noteId = noteDao.create(user.id, note)
                call.respond(HttpStatusCode.Created, noteId)
            }
        }

        // Update existing note
        authenticate {
            put<Notes> {
                val user: User? = getUserFromPrincipal(call)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }

                val note: Note = call.receive()
                val result = noteDao.update(user.id, note)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

        // Delete note
        authenticate {
            delete<Notes.Id> { noteId ->
                val user: User? = getUserFromPrincipal(call)
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }

                val result = noteDao.delete(user.id, noteId.id)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}