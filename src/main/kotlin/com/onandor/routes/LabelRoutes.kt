package com.onandor.routes

import com.onandor.dao.labelDao
import com.onandor.models.Label
import com.onandor.models.User
import com.onandor.plugins.getUserFromPrincipal
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.routing
import io.ktor.server.resources.*
import io.ktor.server.response.*
import java.util.*

@Resource("/labels")
class Labels() {
    @Resource("{id}")
    class Id(val parent: Labels = Labels(), val id: String)
}

fun Application.configureLabelRoutes() {
    routing {
        suspend fun checkIsUserOwner(userId: Int, labelId: UUID): Boolean {
            return labelDao.getAllByUser(userId)
                .any { label -> label.id == labelId && label.userId == userId }
        }

        // Get all labels of a user
        authenticate {
            get<Labels> {
                val user: User = getUserFromPrincipal(call)
                val labels: List<Label> = labelDao.getAllByUser(user.id)
                call.respond(HttpStatusCode.OK, labels)
            }
        }

        // Add a new label
        authenticate {
            post<Labels> {
                val user: User = getUserFromPrincipal(call)
                val label: Label = call.receive()
                // Make sure the user doesn't create a label for somebody else
                val userLabel = label.copy(
                    userId = user.id
                )
                val labelId = labelDao.create(userLabel)
                call.respond(HttpStatusCode.Created, labelId)
            }
        }

        // Update an existing label
        authenticate {
            put<Labels> {
                val user: User = getUserFromPrincipal(call)
                val label: Label = call.receive()
                if (!checkIsUserOwner(user.id, label.id)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }

                val result = labelDao.update(label)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

        // Delete a label
        authenticate {
            delete<Labels.Id> { labelId ->
                val user: User = getUserFromPrincipal(call)
                val labelIdUUID = UUID.fromString(labelId.id)
                if (!checkIsUserOwner(user.id, labelIdUUID)) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val result = labelDao.delete(labelIdUUID)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}