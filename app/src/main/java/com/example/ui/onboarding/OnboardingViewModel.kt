package com.example.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.ThemeMode
import com.example.domain.repository.UserPreferences
import com.example.domain.repository.UserPreferencesRepository
import com.example.domain.repository.ViewMode
import com.example.ui.theme.AccentColorChoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch {
            userPreferencesRepository.setViewMode(mode)
        }
    }

    fun setAccentChoice(accent: AccentColorChoice) {
        viewModelScope.launch {
            userPreferencesRepository.setAccentChoice(accent)
            userPreferencesRepository.setDynamicColor(false)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            // Store hashed PIN
            val hash = pin.hashCode().toString()
            userPreferencesRepository.setPinHash(hash)
        }
    }

    fun setBackupSchedule(schedule: String) {
        viewModelScope.launch {
            userPreferencesRepository.setBackupSchedule(schedule)
        }
    }

    fun completeOnboarding(onFinish: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            onFinish()
        }
    }
}
