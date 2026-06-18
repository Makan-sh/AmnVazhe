package com.example.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // Generate a secure random salt
    fun generateSalt(length: Int = 16): ByteArray {
        val salt = ByteArray(length)
        SecureRandom().nextBytes(salt)
        return salt
    }

    // SHA-256 Hash for Master Password verification
    fun hashPassword(password: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hashedBytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    }

    // PBKDF2 Key Derivation to get standard 256-bit AES key from password + salt
    fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    // AES-GCM Encryption (combines standard 12-byte IV + Ciphertext)
    fun encryptAesGcm(plainText: String, secretKey: ByteArray): String {
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(128, iv)
        val keySpec = SecretKeySpec(secretKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    // AES-GCM Decryption (splits standard 12-byte IV + Ciphertext)
    fun decryptAesGcm(base64Ciphertext: String, secretKey: ByteArray): String {
        val combined = Base64.decode(base64Ciphertext, Base64.NO_WRAP)
        if (combined.size < 12) throw IllegalArgumentException("فایل نامعتبر یا آسیب‌دیده است")
        val iv = ByteArray(12)
        System.arraycopy(combined, 0, iv, 0, 12)
        val encryptedBytes = ByteArray(combined.size - 12)
        System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.size)
        
        val spec = GCMParameterSpec(128, iv)
        val keySpec = SecretKeySpec(secretKey, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
