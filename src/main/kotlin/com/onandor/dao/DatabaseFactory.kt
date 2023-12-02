package com.onandor.dao

import com.onandor.models.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment) {
        val database = Database.connect(
            url = environment.config.property("ktor.database.url").getString(),
            user = environment.config.property("ktor.database.user").getString(),
            driver = environment.config.property("ktor.database.driver").getString(),
            password = environment.config.property("ktor.database.password").getString()
        )
        transaction(database) {
            SchemaUtils.create(Notes)
            SchemaUtils.create(Users)
            SchemaUtils.create(RefreshTokens)
            SchemaUtils.create(Labels)
            SchemaUtils.create(NoteLabels)
            SchemaUtils.create(DeletedNotes)
            SchemaUtils.create(DeletedLabels)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}