package com.example.remotecontrolserver

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import timber.log.Timber

object PairingManager {
    private const val PREFS_NAME = "pairing_prefs"
    private const val KEY_PAIRING = "pairing_code"
    private const val KEY_KEYSET = "aead_keyset"

    suspend fun createPairing(context: Context): Pair<String, ByteArray> = withContext(Dispatchers.IO) {
        val code = generateCode()
        val aead = generateAeadKeyset()
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString(KEY_PAIRING, code).apply()
        prefs.edit().putString(KEY_KEYSET, android.util.Base64.encodeToString(aead, android.util.Base64.NO_WRAP)).apply()
        Timber.i("Created pairing $code")
        code to aead
    }

    fun getPairingCode(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString(KEY_PAIRING, null)
    }

    fun getAeadKey(context: Context): ByteArray? {
        val prefs = getEncryptedPrefs(context)
        val s = prefs.getString(KEY_KEYSET, null) ?: return null
        return android.util.Base64.decode(s, android.util.Base64.NO_WRAP)
    }

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun generateCode(): String {
        val rnd = SecureRandom()
        val num = 100000 + rnd.nextInt(900000)
        return num.toString()
    }

    private fun generateAeadKeyset(): ByteArray {
        // For simplicity we return a raw key bytes derived from Tink key template
        // Here we create a keyset handle and serialize to bytes (in-memory cleartext)
        val handle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM)
        val writer = com.google.crypto.tink.JsonKeysetWriter.withString()
        val baos = java.io.ByteArrayOutputStream()
        com.google.crypto.tink.CleartextKeysetHandle.write(handle, com.google.crypto.tink.JsonKeysetWriter.withOutputStream(baos))
        return baos.toByteArray()
    }

    // Create an AEAD primitive from stored keyset bytes
    fun aeadFromKeysetBytes(keysetBytes: ByteArray): com.google.crypto.tink.Aead? {
        return try {
            val isr = java.io.ByteArrayInputStream(keysetBytes)
            val handle = com.google.crypto.tink.CleartextKeysetHandle.read(com.google.crypto.tink.JsonKeysetReader.withInputStream(isr))
            AeadFactory.getPrimitive(handle)
        } catch (e: Exception) {
            Timber.e(e, "aeadFromKeysetBytes failed")
            null
        }
    }
}