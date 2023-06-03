package com.isel.sensiflow.services

import com.isel.sensiflow.Constants.Security.A_DAY_IN_MILLIS
import com.isel.sensiflow.Constants.Security.DIGEST_ALGORITHM
import com.isel.sensiflow.Constants.Security.SALT_LENGTH
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

private val digest = MessageDigest.getInstance(DIGEST_ALGORITHM)

/**
 * Converts the given [ByteArray] into a [String] with the hexadecimal representation.
 */
fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

/**
 * Hashes the password using SHA-512 with the given salt.
 */
fun hashPassword(password: String, salt: String): String {
    val passPlusSalt = password + salt
    return digest.digest(passPlusSalt.toByteArray()).toHex()
}

/**
 * Generates an expiration date for a token with the received expiration time.
 */
fun generateExpirationDate(expirationTime: Long = A_DAY_IN_MILLIS): Long {
    val now = System.currentTimeMillis()
    return now + expirationTime
}

/**
 * Generates a random UUID.
 */
fun generateUUID() = UUID.randomUUID().toString()

/**
 * Generates a random Salt for the password.
 */
fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(SALT_LENGTH)
    random.nextBytes(salt)
    return salt.toString()
}

/**
 * Converts a [Long] to a [Timestamp].
 */
fun Long.toTimeStamp() =
    Timestamp(this)
