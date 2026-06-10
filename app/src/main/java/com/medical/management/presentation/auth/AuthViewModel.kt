package com.medical.management.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.management.data.model.AuthForm
import com.medical.management.data.model.MedicalUser
import com.medical.management.data.model.UiState
import com.medical.management.domain.repository.MedicalRepository
import com.medical.management.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: MedicalRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UiState<MedicalUser>())
    val state: StateFlow<UiState<MedicalUser>> = _state.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        val error = Validators.login(email, password)
        if (error.isNotBlank()) {
            _state.value = UiState(message = error)
            return@launch
        }
        _state.value = UiState(loading = true)
        repository.login(email, password).fold(
            onSuccess = { _state.value = UiState(data = it, message = "Signed in") },
            onFailure = { _state.value = UiState(message = it.message ?: "Sign in failed") }
        )
    }

    fun register(form: AuthForm) = viewModelScope.launch {
        val error = Validators.registration(form)
        if (error.isNotBlank()) {
            _state.value = UiState(message = error)
            return@launch
        }
        _state.value = UiState(loading = true)
        repository.register(form).fold(
            onSuccess = { _state.value = UiState(data = it, message = "Account created successfully") },
            onFailure = { 
                val errorMsg = when {
                    it.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ->
                        "Firebase Authentication is not enabled for this project. Enable Email/Password sign-in in Firebase Console."
                    it.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Check your connection and try again."
                    it.message?.contains("permission", ignoreCase = true) == true ->
                        "Profile could not be saved. Check Firestore security rules for users."
                    it.message?.contains("email address is already", ignoreCase = true) == true ->
                        "An account already exists for this email."
                    else -> "Registration failed. ${it.message ?: "Please try again."}"
                }
                _state.value = UiState(message = errorMsg)
            }
        )
    }

    fun reset(email: String) = viewModelScope.launch {
        repository.sendPasswordReset(email).fold(
            onSuccess = { _state.value = UiState(message = "Password reset email sent") },
            onFailure = { _state.value = UiState(message = it.message ?: "Unable to send reset email") }
        )
    }
}
