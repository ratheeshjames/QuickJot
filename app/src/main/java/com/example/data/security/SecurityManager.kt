package com.example.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

class SecurityManager(private val context: Context) {

    private var lastActivityTimestamp: Long = 0L
    private var isUnlocked: Boolean = false

    fun isAppUnlocked(autoLockTimeoutSeconds: Int, pinHash: String?, biometricEnabled: Boolean): Boolean {
        // If no PIN and no biometrics enabled, always unlocked
        if (pinHash == null && !biometricEnabled) {
            return true
        }

        if (!isUnlocked) {
            return false
        }

        if (autoLockTimeoutSeconds <= 0) {
            // Immediate lock on backgrounding
            return isUnlocked
        }

        val elapsed = (System.currentTimeMillis() - lastActivityTimestamp) / 1000
        if (elapsed > autoLockTimeoutSeconds) {
            isUnlocked = false
            return false
        }

        return true
    }

    fun markUnlocked() {
        isUnlocked = true
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun lock() {
        isUnlocked = false
    }

    fun recordUserActivity() {
        lastActivityTimestamp = System.currentTimeMillis()
    }

    fun canAuthenticateWithBiometrics(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Unlock QuickJot",
        subtitle: String = "Confirm your identity",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    markUnlocked()
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Don't report user cancel as a hard failure
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed. Try again.")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .build()

        prompt.authenticate(promptInfo)
    }

    companion object {
        fun hashPin(pin: String): String {
            val salt = "quickjot_salt_2026"
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest((salt + pin).toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun verifyPin(pin: String, storedHash: String?): Boolean {
            if (storedHash == null) return false
            return hashPin(pin) == storedHash
        }
    }
}
