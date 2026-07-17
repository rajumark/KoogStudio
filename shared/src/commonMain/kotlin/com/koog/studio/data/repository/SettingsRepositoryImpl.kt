package com.koog.studio.data.repository

import com.koog.studio.domain.repository.SettingsRepository
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.util.Base64
import java.security.SecureRandom

class SettingsRepositoryImpl : SettingsRepository {

    private val prefs: Preferences = Preferences.userNodeForPackage(SettingsRepository::class.java)

    companion object {
        private const val KEY_PREFIX = "api_key_"
        private const val KEY_ALIAS = "koog_api_key_encryption"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        val PROVIDERS = listOf("OpenAI", "Anthropic", "Google")
    }

    private fun getOrCreateKey(): SecretKey {
        val prefsKey = prefs.get("encryption_key", null)
        if (prefsKey != null) {
            val decoded = Base64.getDecoder().decode(prefsKey)
            return javax.crypto.spec.SecretKeySpec(decoded, "AES")
        }
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val key = keyGen.generateKey()
        prefs.put("encryption_key", Base64.getEncoder().encodeToString(key.encoded))
        return key
    }

    override fun getApiKey(provider: String): String? {
        val encrypted = prefs.get("$KEY_PREFIX$provider", null) ?: return null
        return try {
            val decoded = Base64.getDecoder().decode(encrypted)
            val iv = decoded.copyOfRange(0, GCM_IV_LENGTH)
            val ciphertext = decoded.copyOfRange(GCM_IV_LENGTH, decoded.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)
            val plainBytes = cipher.doFinal(ciphertext)
            String(plainBytes, Charsets.UTF_8).takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            prefs.remove("$KEY_PREFIX$provider")
            null
        }
    }

    override fun setApiKey(provider: String, key: String) {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(), spec)
        val encrypted = cipher.doFinal(key.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        prefs.put("$KEY_PREFIX$provider", Base64.getEncoder().encodeToString(combined))
    }

    override fun removeApiKey(provider: String) {
        prefs.remove("$KEY_PREFIX$provider")
    }

    override fun isConfigured(provider: String): Boolean {
        return getApiKey(provider) != null
    }

    override fun getConfiguredProviders(): List<String> {
        return PROVIDERS.filter { isConfigured(it) }
    }
}
