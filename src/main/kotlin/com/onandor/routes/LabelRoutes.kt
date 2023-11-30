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

    @Resource("sync")
    class Sync(val parent: Labels = Labels())
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
                call.respond(HttpStatusCode.Created, hashMapOf("id" to labelId))
            }
        }

        // Update an existing label
        authenticate {
            put<Labels> {
                val user: User = getUserFromPrincipal(call)
                val label: Label = call.receive()
                if (labelDao.getById(label.id) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@put
                }
                if (!checkIsUserOwner(user.id, label.id)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }
                labelDao.update(label)
                call.respond(HttpStatusCode.OK)
            }
        }

        // Update or insert modified or new labels, delete missing labels
        authenticate {
            put<Labels.Sync> {
                val user: User = getUserFromPrincipal(call)
                val labels: List<Label> = call.receive()
                val ownedLabels = labels.filter { label -> label.userId == user.id }
                labelDao.upsertAllIfNewer(ownedLabels)

                val userLabels = labelDao.getAllByUser(user.id)
                val deletedLabels = userLabels.filterNot { userLabel ->
                    ownedLabels.any { ownedLabel -> userLabel.id == ownedLabel.id }
                }
                labelDao.deleteAllByIds(deletedLabels.map { label -> label.id })
                call.respond(HttpStatusCode.OK)
            }
        }

        // Delete a label
        authenticate {
            delete<Labels.Id> { labelId ->
                val user: User = getUserFromPrincipal(call)
                val labelIdUUID: UUID
                try {
                    labelIdUUID = UUID.fromString(labelId.id)
                } catch (e: java.lang.IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }

                if (labelDao.getById(labelIdUUID) == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@delete
                }
                if (!checkIsUserOwner(user.id, labelIdUUID)) {
                    call.respond(HttpStatusCode.Forbidden)
                    return@delete
                }
                labelDao.delete(labelIdUUID)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}