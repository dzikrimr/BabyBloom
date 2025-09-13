package com.example.bubtrack.data.livekit

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenGenerator @Inject constructor() {

    // Replace these with your actual LiveKit credentials
    private val API_KEY = "API2REB4cSfMr7Q"
    private val API_SECRET = "iW7uah0tqsVJgjNsbyEJdEqGe3XCidQa48xBDPKUe24"

    fun generateAccessToken(
        roomName: String,
        identity: String,
        isHost: Boolean = false
    ): String {
        val now = Date()
        val expiry = Date(now.time + 24 * 60 * 60 * 1000) // 24 hours

        val algorithm = Algorithm.HMAC256(API_SECRET)

        // Create video grants - this is the key fix
        val videoGrants = mutableMapOf<String, Any>()
        videoGrants["roomJoin"] = true
        videoGrants["room"] = roomName

        if (isHost) {
            // Host permissions
            videoGrants["canPublish"] = true
            videoGrants["canSubscribe"] = true
            videoGrants["canPublishData"] = true
        } else {
            // Viewer permissions
            videoGrants["canPublish"] = false
            videoGrants["canSubscribe"] = true
            videoGrants["canPublishData"] = false
        }

        videoGrants["canSubscribeData"] = true

        return JWT.create()
            .withIssuer(API_KEY)
            .withSubject(identity)
            .withAudience("livekit")
            .withExpiresAt(expiry)
            .withIssuedAt(now)
            .withNotBefore(now)
            .withClaim("video", videoGrants) // This is the correct structure
            .sign(algorithm)
    }
}