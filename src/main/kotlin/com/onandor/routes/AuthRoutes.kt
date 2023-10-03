package com.onandor.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.onandor.dao.refreshTokenDao
import com.onandor.dao.userDao
import com.onandor.models.RefreshToken
import com.onandor.models.User
import com.onandor.plugins.JwkProviderFactory
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.util.*

@Resource("/auth")
class Auth() {
    @Resource("login")
    class Login(val parent: Auth = Auth())

    @Resource("register")
    class Register(val parent: Auth = Auth())

    @Resource("refresh")
    class Refresh(val parent: Auth = Auth())
}

fun Application.configureAuthRoutes() {
    val jwkProvider: JwkProvider = JwkProviderFactory.jwkProvider!!
    val privateKeyString = JwkProviderFactory.jwtPrivateKey
    val jwtAudience = JwkProviderFactory.jwtAudience
    val jwtIssuer = JwkProviderFactory.jwtIssuer
    val accessTokenExpiration = JwkProviderFactory.accessTokenExpiration
    val refreshTokenExpiration = JwkProviderFactory.refreshTokenExpiration

    fun createAccessToken(user: User): String {
        val currentTime = System.currentTimeMillis()
        val publicKey = jwkProvider.get("whQeRd1vdutXo11e7zAi1").publicKey
        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
        val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(currentTime + Duration.ofMinutes(accessTokenExpiration).toMillis()))
            .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
    }

    suspend fun createRefreshToken(user: User, deviceId: UUID): String {
        val refreshTokenValue = UUID.randomUUID()
        val currentTime = System.currentTimeMillis()
        val refreshToken = RefreshToken(
            userId = user.id,
            deviceId = deviceId,
            tokenValue = refreshTokenValue,
            expiresAt = Date(currentTime + Duration.ofDays(refreshTokenExpiration).toMillis()).time,
            valid = true
        )
        refreshTokenDao.create(refreshToken)
        return refreshTokenValue.toString()
    }

    routing {
        staticResources("/.well-known", "well-known")

        post<Auth.Login> {
            val authUser: User = call.receive()
            if (authUser.deviceId == null) {
                call.respond(HttpStatusCode.BadRequest, "Device ID can not be blank.")
            }

            val dbUser: User? = userDao.getByEmail(authUser.email)
            if (dbUser == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val hashResult = BCrypt.verifyer().verify(authUser.password.toCharArray(), dbUser.password)
            if (!hashResult.verified) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            refreshTokenDao.deleteAllByUserDevice(dbUser.id, authUser.deviceId!!)
            val accessToken: String = createAccessToken(dbUser)
            val refreshToken: String = createRefreshToken(dbUser, authUser.deviceId)
            call.respond(HttpStatusCode.OK, hashMapOf("access_token" to accessToken, "refresh_token" to refreshToken))
        }

        post<Auth.Register> {
            val user: User = call.receive()
            val existingUser: User? = userDao.getByEmail(user.email)
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            val passwordHash: String = BCrypt
                .withDefaults()
                .hashToString(12, user.password.toCharArray())

            val userId = userDao.create(user.email, passwordHash)
            call.respond(HttpStatusCode.Created, hashMapOf("id" to userId, "email" to user.email))
        }

        post<Auth.Refresh> {
            val refreshTokenString = call.parameters["refreshToken"]
            if (refreshTokenString.isNullOrEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Refresh token can not be blank.")
            }
            val oldRefreshTokenValue: UUID = UUID.fromString(refreshTokenString)
            val oldRefreshToken: RefreshToken? = refreshTokenDao.getByTokenValue(oldRefreshTokenValue)
            val currentTime = System.currentTimeMillis()

            if (oldRefreshToken == null) {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }
            else if (!oldRefreshToken.valid || oldRefreshToken.expiresAt < currentTime) {
                refreshTokenDao.deleteAllByUserDevice(oldRefreshToken.userId, oldRefreshToken.deviceId)
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val user: User? = userDao.getById(oldRefreshToken.userId)
            if (user == null) {
                refreshTokenDao.deleteAllByUser(oldRefreshToken.userId)
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            refreshTokenDao.invalidate(oldRefreshToken)
            val accessToken = createAccessToken(user)
            val newRefreshToken: String = createRefreshToken(user, oldRefreshToken.deviceId)
            call.respond(HttpStatusCode.OK, hashMapOf("access_token" to accessToken, "refresh_token" to newRefreshToken))
        }

        authenticate {
            delete<Auth> {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asInt()

                val result = userDao.delete(userId)
                if (result > 0)
                    call.respond(HttpStatusCode.OK)
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }

        // TODO: for testing only
        get<Auth> {
            val users = userDao.getAll()
            call.respond(HttpStatusCode.OK, users)
        }

        // TODO: for testing only
        get<Auth.Refresh> {
            val refreshTokens = refreshTokenDao.getAll()
            call.respond(HttpStatusCode.OK, refreshTokens)
        }
    }
}