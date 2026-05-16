package com.geomeasure.pro.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {
    private const val KEY_ALIAS = "GeoMeasureDbKey"
    private const val KEY_SIZE = 256
    private const val PREFS = "gm_secure_prefs"
    private const val PREF_KEY = "db_iv_wrapped"

    fun getOrCreateDatabaseKey(context: Context): ByteArray {
        try {
            val keyStore = try {
                KeyStore.getInstance("AndroidKeyStore").also { it.load(null) }
            } catch (e: Exception) {
                return fallbackKey(context)
            }

            try {
                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    val spec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(KEY_SIZE)
                        .setUserAuthenticationRequired(false)
                        .build()
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                        .apply { init(spec) }.generateKey()
                }
            } catch (e: Exception) {
                return fallbackKey(context)
            }

            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (!prefs.contains(PREF_KEY)) {
                try {
                    val rawKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
                    val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                        ?: return fallbackKey(context)
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    val encrypted = cipher.doFinal(rawKey)
                    val combined = cipher.iv + encrypted
                    prefs.edit().putString(PREF_KEY, Base64.encodeToString(combined, Base64.NO_WRAP)).apply()
                } catch (e: Exception) {
                    return fallbackKey(context)
                }
            }

            val prefValue = prefs.getString(PREF_KEY, "")
            if (prefValue.isNullOrEmpty()) {
                return regenerateKey(context, keyStore, prefs)
            }
            val combined = try {
                Base64.decode(prefValue, Base64.NO_WRAP)
            } catch (_: IllegalArgumentException) {
                return regenerateKey(context, keyStore, prefs)
            }
            if (combined.size < 13) {
                return regenerateKey(context, keyStore, prefs)
            }
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                ?: return fallbackKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            return cipher.doFinal(encrypted)
        } catch (e: Exception) {
            return fallbackKey(context)
        }
    }

    private fun regenerateKey(
        context: Context, keyStore: KeyStore, prefs: android.content.SharedPreferences
    ): ByteArray {
        try {
            prefs.edit().remove(PREF_KEY).apply()
            val rawKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                ?: return fallbackKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(rawKey)
            val combined = cipher.iv + encrypted
            prefs.edit().putString(PREF_KEY, Base64.encodeToString(combined, Base64.NO_WRAP)).apply()
            return rawKey
        } catch (_: Exception) {
            return fallbackKey(context)
        }
    }

    private fun fallbackKey(context: Context): ByteArray {
        // Graceful fallback: generate in-memory key when Android KeyStore fails
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString("fallback_key", null)
        if (existing != null) {
            return try {
                Base64.decode(existing, Base64.NO_WRAP)
            } catch (_: Exception) {
                ByteArray(32).also { SecureRandom().nextBytes(it) }.also {
                    prefs.edit().putString("fallback_key", Base64.encodeToString(it, Base64.NO_WRAP)).apply()
                }
            }
        }
        return ByteArray(32).also { SecureRandom().nextBytes(it) }.also {
            prefs.edit().putString("fallback_key", Base64.encodeToString(it, Base64.NO_WRAP)).apply()
        }
    }
}
