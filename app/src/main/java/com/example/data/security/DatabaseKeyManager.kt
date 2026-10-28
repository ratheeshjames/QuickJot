package com.example.data.security

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

/**
 * Manages the Keystore-protected encryption key for the SQLCipher database.
 * Protects database master key with hardware-backed Keystore AES-256 GCM encryption.
 */
class DatabaseKeyManager(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getOrCreateDatabaseKey(): ByteArray {
        val encryptedKeyB64 = prefs.getString(PREF_ENCRYPTED_DB_KEY, null)
        val ivB64 = prefs.getString(PREF_IV, null)

        return if (encryptedKeyB64 != null && ivB64 != null) {
            try {
                decryptKey(
                    encryptedKey = Base64.decode(encryptedKeyB64, Base64.NO_WRAP),
                    iv = Base64.decode(ivB64, Base64.NO_WRAP)
                )
            } catch (e: Exception) {
                // If Keystore key was invalidated, regenerate key
                generateAndStoreNewKey()
            }
        } else {
            generateAndStoreNewKey()
        }
    }

    private fun generateAndStoreNewKey(): ByteArray {
        // Generate a 256-bit random passphrase for SQLCipher
        val rawKey = ByteArray(32)
        SecureRandom().nextBytes(rawKey)

        val masterSecretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterSecretKey)
        val iv = cipher.iv
        val encryptedKey = cipher.doFinal(rawKey)

        prefs.edit()
            .putString(PREF_ENCRYPTED_DB_KEY, Base64.encodeToString(encryptedKey, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()

        return rawKey
    }

    private fun decryptKey(encryptedKey: ByteArray, iv: ByteArray): ByteArray {
        val masterSecretKey = getOrCreateMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, masterSecretKey, spec)
        return cipher.doFinal(encryptedKey)
    }

    private fun getOrCreateMasterKey(): SecretKey {
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "quickjot_db_master_key"
        private const val PREFS_NAME = "quickjot_security_prefs"
        private const val PREF_ENCRYPTED_DB_KEY = "encrypted_db_key"
        private const val PREF_IV = "db_key_iv"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }
}
