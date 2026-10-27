package com.example.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AuthSecurityManager {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    private val secureRandom = SecureRandom()

    fun generateSaltBase64(): String {
        val salt = ByteArray(SALT_BYTES)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: CharArray, saltBase64: String): String {
        val salt = Base64.getDecoder().decode(saltBase64)
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val hashBytes = factory.generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(hashBytes)
        } finally {
            spec.clearPassword()
        }
    }

    fun verifyPassword(password: CharArray, saltBase64: String, expectedHashBase64: String): Boolean {
        return try {
            val actualHashBase64 = hashPassword(password, saltBase64)
            val actualBytes = Base64.getDecoder().decode(actualHashBase64)
            val expectedBytes = Base64.getDecoder().decode(expectedHashBase64)
            MessageDigest.isEqual(actualBytes, expectedBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.length < 5 || !trimmed.contains("@") || !trimmed.contains(".")) return false
        val parts = trimmed.split("@")
        return parts.size == 2 && parts[0].isNotBlank() && parts[1].contains(".") && !parts[1].startsWith(".") && !parts[1].endsWith(".")
    }

    fun validatePasswordStrength(password: String): String? {
        if (password.length < 6) {
            return "Password must be at least 6 characters long."
        }
        return null
    }
}
