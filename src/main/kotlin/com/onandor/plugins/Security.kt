package com.onandor.plugins

import com.onandor.dao.userDao
import com.onandor.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    JwkProviderFactory.init(environment)
    val jwkProvider = JwkProviderFactory.jwkProvider
    val jwtRealm = JwkProviderFactory.jwtRealm
    val jwtIssuer = JwkProviderFactory.jwtIssuer
    val jwtAudience = JwkProviderFactory.jwtAudience

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                jwkProvider = jwkProvider!!,
                issuer = jwtIssuer
            ) {
                acceptLeeway(3)
            }
            validate { credential ->
                val userId: Int = credential.payload.claims["userId"]?.asInt() ?: -1
                val user: User? = userDao.getById(userId)
                if (credential.payload.audience.contains(jwtAudience) && user != null) {
                    JWTPrincipal(credential.payload)
                }
                else {
                    null
                }
            }
            challenge { _, _ ->
                val authHeader = call.request.headers["Authorization"]
                if (authHeader.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "Authorization header can not be blank.")
                }
                call.respond(HttpStatusCode.Unauthorized, "Access token is not valid or has expired.")
            }
        }
    }
}

suspend fun getUserFromPrincipal(call: ApplicationCall): User {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal!!.payload.getClaim("userId").asInt()
    return userDao.getById(userId)!!
}
