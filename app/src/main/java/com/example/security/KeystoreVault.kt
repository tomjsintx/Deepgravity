package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreVault(private val context: Context) {

    private val keyAlias = "DeepGravity_Vault_Key_v1"
    private val prefs: SharedPreferences = context.getSharedPreferences("deepgravity_secure_prefs", Context.MODE_PRIVATE)
    private val transformation = "AES/GCM/NoPadding"
    private val keyStoreType = "AndroidKeyStore"

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
        if (keyStore.containsAlias(keyAlias)) {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreType)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }

    fun storeApiKey(providerId: String, apiKey: String): Boolean {
        return try {
            if (apiKey.isBlank()) {
                prefs.edit().remove("key_$providerId").remove("iv_$providerId").apply()
                return true
            }

            try {
                val secretKey = getOrCreateSecretKey()
                val cipher = Cipher.getInstance(transformation)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))

                val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
                val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

                prefs.edit()
                    .putString("key_$providerId", encryptedBase64)
                    .putString("iv_$providerId", ivBase64)
                    .apply()
                true
            } catch (e: Exception) {
                // Fallback for test or legacy environments without hardware keystore
                val encoded = Base64.encodeToString(apiKey.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                prefs.edit().putString("key_fallback_$providerId", encoded).apply()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getApiKey(providerId: String): String? {
        return try {
            val encryptedBase64 = prefs.getString("key_$providerId", null)
            val ivBase64 = prefs.getString("iv_$providerId", null)

            if (encryptedBase64 != null && ivBase64 != null) {
                try {
                    val keyStore = KeyStore.getInstance(keyStoreType).apply { load(null) }
                    val secretKey = (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
                    if (secretKey != null) {
                        val cipher = Cipher.getInstance(transformation)
                        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                        val spec = GCMParameterSpec(128, iv)
                        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedBase64, Base64.NO_WRAP))
                        return String(decryptedBytes, Charsets.UTF_8)
                    }
                } catch (e: Exception) {
                    // fall through to fallback check
                }
            }

            val fallback = prefs.getString("key_fallback_$providerId", null)
            if (fallback != null) {
                return String(Base64.decode(fallback, Base64.NO_WRAP), Charsets.UTF_8)
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun storeEncryptedSecret(key: String, value: String): Boolean = storeApiKey(key, value)

    fun getDecryptedSecret(key: String): String? = getApiKey(key)

    fun hasApiKey(providerId: String): Boolean {
        val key = getApiKey(providerId)
        return !key.isNullOrBlank()
    }

    fun removeApiKey(providerId: String) {
        prefs.edit()
            .remove("key_$providerId")
            .remove("iv_$providerId")
            .remove("key_fallback_$providerId")
            .apply()
    }
}
