package com.medical.management.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.management.data.model.MedicalUser
import com.medical.management.domain.repository.MedicalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionState(
    val loading: Boolean = true,
    val user: MedicalUser? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: MedicalRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user ->
                _state.value = SessionState(loading = false, user = user)
            }
        }
    }

    fun logout() = viewModelScope.launch { repository.logout() }
}
