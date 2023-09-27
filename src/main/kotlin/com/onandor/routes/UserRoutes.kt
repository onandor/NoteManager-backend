package com.onandor.routes

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.onandor.models.User
import com.onandor.plugins.JwkProviderFactory
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

@Resource("/auth")
class Auth() {
    @Resource("login")
    class Login(val parent: Auth = Auth())
}

fun Application.configureUserRoutes() {
    val jwkProvider: JwkProvider = JwkProviderFactory.getJwkProvider()
    val privateKeyString = JwkProviderFactory.getJwtPrivateKey()
    val jwtAudience = JwkProviderFactory.getJwtAudience()
    val jwtIssuer = JwkProviderFactory.getJwtIssuer()

    routing {
        staticResources("/.well-known", "well-known")

        post<Auth.Login> {
            val user: User = call.receive()
            // TODO: username and password check
            val publicKey = jwkProvider.get("whQeRd1vdutXo11e7zAi1").publicKey
            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
            val token = JWT.create()
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .withClaim("email", user.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 1200000))
                .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
            call.respond(hashMapOf("token" to token))
        }
    }
}