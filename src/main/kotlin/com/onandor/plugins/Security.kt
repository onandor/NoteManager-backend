package com.onandor.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    JwkProviderFactory.init(environment)
    val jwkProvider = JwkProviderFactory.getJwkProvider()
    val jwtRealm = JwkProviderFactory.getJwtRealm()
    val jwtIssuer = JwkProviderFactory.getJwtIssuer()
    val jwtAudience = JwkProviderFactory.getJwtAudience()

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                jwkProvider = jwkProvider,
                issuer = jwtIssuer
            ) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                else
                    null
            }
            challenge { defaultScheme, realm ->  
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired.")
            }
        }
    }
}
