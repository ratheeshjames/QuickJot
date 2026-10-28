package com.example.ui.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.security.SecurityManager
import com.example.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LockUiState(
    val enteredPin: String = "",
    val errorMessage: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isUnlocked: Boolean = false,
    val title: String = "Unlock QuickJot"
)

class LockViewModel(
    private val securityManager: SecurityManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reason: String
) : ViewModel() {

    private val enteredPinFlow = MutableStateFlow("")
    private val errorMessageFlow = MutableStateFlow<String?>(null)
    private val isUnlockedFlow = MutableStateFlow(false)

    val uiState: StateFlow<LockUiState> = combine(
        enteredPinFlow,
        errorMessageFlow,
        isUnlockedFlow
    ) { pin, error, unlocked ->
        LockUiState(
            enteredPin = pin,
            errorMessage = error,
            isBiometricAvailable = securityManager.canAuthenticateWithBiometrics(),
            isUnlocked = unlocked,
            title = if (reason.startsWith("note_lock")) "Locked Note" else "Unlock QuickJot"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LockUiState()
    )

    fun onDigit(digit: String, onSuccess: () -> Unit) {
        if (enteredPinFlow.value.length < 4) {
            val newPin = enteredPinFlow.value + digit
            enteredPinFlow.value = newPin
            errorMessageFlow.value = null

            if (newPin.length == 4) {
                verifyPin(newPin, onSuccess)
            }
        }
    }

    fun onBackspace() {
        if (enteredPinFlow.value.isNotEmpty()) {
            enteredPinFlow.value = enteredPinFlow.value.dropLast(1)
            errorMessageFlow.value = null
        }
    }

    private fun verifyPin(pin: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.preferencesFlow.first()
            if (SecurityManager.verifyPin(pin, prefs.pinHash)) {
                securityManager.markUnlocked()
                isUnlockedFlow.value = true
                onSuccess()
            } else {
                errorMessageFlow.value = "Incorrect PIN. Try again."
                enteredPinFlow.value = ""
            }
        }
    }

    fun triggerBiometric(activity: FragmentActivity, onSuccess: () -> Unit) {
        securityManager.promptBiometric(
            activity = activity,
            title = if (reason.startsWith("note_lock")) "Unlock Note" else "Unlock QuickJot",
            onSuccess = {
                securityManager.markUnlocked()
                isUnlockedFlow.value = true
                onSuccess()
            },
            onError = { msg ->
                errorMessageFlow.value = msg
            }
        )
    }
}
