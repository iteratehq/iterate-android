package com.iteratehq.iterate.data.local

import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class CryptoManager(
    private val enabled: Boolean,
) {
    private val transformation = "AES/GCM/NoPadding"
    private val keyAlias = "Iterate_Encryption_Key"
    private val ivSize = 12

    @RequiresApi(Build.VERSION_CODES.M)
    fun encrypt(value: String?): String? {
        if (!enabled || value == null) return value
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val output = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(ciphertext, 0, output, iv.size, ciphertext.size)
        return Base64.encodeToString(output, Base64.NO_WRAP)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun decrypt(value: String?): String? {
        if (!enabled || value == null) return value
        return try {
            val decoded = Base64.decode(value, Base64.NO_WRAP)
            val iv = decoded.copyOfRange(0, ivSize)
            val cipherText = decoded.copyOfRange(ivSize, decoded.size)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(cipherText)
            String(plain, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSecretKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = (ks.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
        if (existing != null) return existing
        val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        val spec =
            android.security.keystore.KeyGenParameterSpec
                .Builder(
                    keyAlias,
                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
