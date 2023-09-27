package com.onandor.plugins

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwk.UrlJwkProvider
import io.ktor.server.application.*
import java.net.URL
import java.util.concurrent.TimeUnit

object JwkProviderFactory {

    private var jwkProvider: JwkProvider? = null
    private var jwtPrivateKey: String = ""
    private var jwtIssuer: String = ""
    private var jwtAudience: String = ""
    private var jwtRealm: String = ""

    fun init(environment: ApplicationEnvironment) {
        jwtPrivateKey = environment.config.property("ktor.jwt.privateKey").getString()
        jwtIssuer = environment.config.property("ktor.jwt.issuer").getString()
        jwtAudience = environment.config.property("ktor.jwt.audience").getString()
        jwtRealm = environment.config.property("ktor.jwt.realm").getString()

        jwkProvider = JwkProviderBuilder(jwtIssuer)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    }

    fun getJwkProvider(): JwkProvider {
        if (jwkProvider == null)
            throw Exception("JwkProviderFactory is not initialized")
        return jwkProvider!!
    }

    fun getJwtPrivateKey(): String {
        if (jwtPrivateKey.isEmpty())
            throw Exception("JwkProviderFactory is not initialized")
        return jwtPrivateKey
    }

    fun getJwtRealm() : String {
        if (jwtRealm.isEmpty())
            throw Exception("JwkProviderFactory is not initialized")
        return jwtRealm
    }

    fun getJwtIssuer() : String {
        if (jwtIssuer.isEmpty())
            throw Exception("JwkProviderFactory is not initialized")
        return jwtIssuer
    }

    fun getJwtAudience() : String {
        if (jwtAudience.isEmpty())
            throw Exception("JwkProviderFactory is not initialized")
        return jwtAudience
    }
}