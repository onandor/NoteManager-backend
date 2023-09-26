package com.onandor.routes

import com.onandor.dao.noteDao
import com.onandor.models.Note
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.configureNoteRoutes() {
    routing {
        route("/notes") {
            // Get all notes of a user
            get {
                val notes: List<Note> = noteDao.getAllByUser("todo")
                call.respond(HttpStatusCode.OK, notes)
            }

            // Get a specific note of a user by note id
            get("{noteId}") {
                val noteId = call.parameters.getOrFail("noteId")
                val note: Note? = noteDao.getById("todo", noteId)
                if (note == null)
                    call.respond(HttpStatusCode.NotFound)
                else
                    call.respond(HttpStatusCode.OK, note)
            }

            // Add new note
            post {
                val note: Note = call.receive()
                val noteId = noteDao.create("todo", note)
                call.respond(HttpStatusCode.Created, noteId)
            }

            // Update existing note
            put {
                //val noteId = call.parameters.getOrFail("noteId")
                val note: Note = call.receive()
                noteDao.update("todo", note)
                call.respond(HttpStatusCode.OK)
            }

            delete("{noteId}") {
                val noteId = call.parameters.getOrFail("noteId")
                noteDao.delete("todo", noteId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}