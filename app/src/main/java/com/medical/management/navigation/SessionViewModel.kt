package com.medical.management.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.management.data.model.AuthSession
import com.medical.management.domain.repository.MedicalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: MedicalRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthSession())
    val state: StateFlow<AuthSession> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authSession.collect { session -> _state.value = session }
        }
    }

    fun logout() = viewModelScope.launch { repository.logout() }
}
