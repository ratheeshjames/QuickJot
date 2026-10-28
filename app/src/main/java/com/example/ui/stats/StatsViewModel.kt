package com.example.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.NotesRepository
import com.example.domain.repository.QuickJotStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    notesRepository: NotesRepository
) : ViewModel() {

    val statsState: StateFlow<QuickJotStats> = notesRepository.statsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuickJotStats()
        )
}
