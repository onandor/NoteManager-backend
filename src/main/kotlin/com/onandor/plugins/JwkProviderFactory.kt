package com.onandor.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwk.UrlJwkProvider
import io.ktor.server.application.*
import java.net.URL
import java.util.concurrent.TimeUnit

object JwkProviderFactory {

    var jwkProvider: JwkProvider? = null
        private set
    var jwtPrivateKey: String = ""
        private set
    var jwtIssuer: String = ""
        private set
    var jwtAudience: String = ""
        private set
    var jwtRealm: String = ""
        private set
    var accessTokenExpiration: Long = 10
        private set
    var refreshTokenExpiration: Long = 90
        private set

    fun init(environment: ApplicationEnvironment) {
        jwtPrivateKey = environment.config.property("ktor.security.jwt.privateKey").getString()
        jwtIssuer = environment.config.property("ktor.security.jwt.issuer").getString()
        jwtAudience = environment.config.property("ktor.security.jwt.audience").getString()
        jwtRealm = environment.config.property("ktor.security.jwt.realm").getString()
        accessTokenExpiration = environment.config
            .property("ktor.security.jwt.accessTokenExpiration").getString().toLong()
        refreshTokenExpiration = environment.config
            .property("ktor.security.jwt.refreshTokenExpiration").getString().toLong()

        jwkProvider = JwkProviderBuilder(jwtIssuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    }
}