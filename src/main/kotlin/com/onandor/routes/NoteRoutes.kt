package com.onandor.routes

import com.onandor.dao.noteDao
import com.onandor.models.Note
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
        // Get all notes of a user
        authenticate {
            get<Notes> {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                println(expiresAt)
                val notes: List<Note> = noteDao.getAllByUser("todo")
                call.respond(HttpStatusCode.OK, notes)
            }
        }

        // Get a specific note of a user by note id
        get<Notes.Id> {noteId ->
            val note: Note? = noteDao.getById("todo", noteId.id)
            if (note == null)
                call.respond(HttpStatusCode.NotFound)
            else
                call.respond(HttpStatusCode.OK, note)
        }

        // Add new note
        post<Notes> {
            val note: Note = call.receive()
            val noteId = noteDao.create("todo", note)
            call.respond(HttpStatusCode.Created, noteId)
        }

        // Update existing note
        put<Notes> {
            val note: Note = call.receive()
            val result = noteDao.update("todo", note)
            if (result > 0)
                call.respond(HttpStatusCode.OK)
            else
                call.respond(HttpStatusCode.NotFound)
        }

        // Delete note
        delete<Notes.Id> { noteId ->
            val result = noteDao.delete("todo", noteId.id)
            if (result > 0)
                call.respond(HttpStatusCode.OK)
            else
                call.respond(HttpStatusCode.NotFound)
        }
    }
}