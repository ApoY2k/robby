package apoy2k.robby

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val derivedKeyLength = 512
private const val iterations = 4096
private val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
private val random = SecureRandom.getInstance("SHA1PRNG")

fun generateSalt(): ByteArray {
    val salt = ByteArray(8)
    random.nextBytes(salt)
    return salt
}

fun encryptPassword(password: String, salt: ByteArray): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength)
    return factory.generateSecret(spec).encoded
}

fun isAuthenticated(attempt: String, encrypted: ByteArray, salt: ByteArray): Boolean {
    val encryptedAttempt = encryptPassword(attempt, salt)
    return encrypted.contentEquals(encryptedAttempt)
}
