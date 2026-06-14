package com.medical.management.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
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
            onSuccess = { _state.value = UiState(data = it, message = "Signed in. Loading your workspace...") },
            onFailure = { _state.value = UiState(message = authMessage(it, "Sign in failed. Please try again.")) }
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
            onFailure = { _state.value = UiState(message = authMessage(it, "Registration failed. Please try again.")) }
        )
    }

    fun reset(email: String) = viewModelScope.launch {
        val error = Validators.email(email)
        if (error.isNotBlank()) {
            _state.value = UiState(message = error)
            return@launch
        }
        _state.value = UiState(loading = true)
        repository.sendPasswordReset(email).fold(
            onSuccess = { _state.value = UiState(message = "If an account exists for this email, a password reset link has been sent.") },
            onFailure = { _state.value = UiState(message = authMessage(it, "Unable to send reset email. Please try again.")) }
        )
    }

    private fun authMessage(error: Throwable, fallback: String): String {
        val message = error.message.orEmpty()
        val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
        return when {
            code == "ERROR_INVALID_CREDENTIAL" ||
                code == "ERROR_WRONG_PASSWORD" ||
                code == "ERROR_USER_NOT_FOUND" ->
                "The email or password is incorrect."
            code == "ERROR_INVALID_EMAIL" ->
                "Enter a valid email address."
            code == "ERROR_USER_DISABLED" ->
                "This account has been disabled. Contact support for help."
            code == "ERROR_TOO_MANY_REQUESTS" ->
                "Too many attempts. Please wait a moment and try again."
            code == "ERROR_EMAIL_ALREADY_IN_USE" ->
                "An account already exists for this email."
            code == "ERROR_NETWORK_REQUEST_FAILED" ->
                "Network error. Check your connection and try again."
            message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Email/password sign-in is not enabled for this Firebase project."
            message.contains("INVALID_EMAIL", ignoreCase = true) ->
                "Enter a valid email address."
            message.contains("network", ignoreCase = true) ->
                "Network error. Check your connection and try again."
            message.contains("password is invalid", ignoreCase = true) ||
                message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                message.contains("INVALID_PASSWORD", ignoreCase = true) ||
                message.contains("no user record", ignoreCase = true) ||
                message.contains("USER_NOT_FOUND", ignoreCase = true) ->
                "The email or password is incorrect."
            message.contains("disabled", ignoreCase = true) ->
                "This account has been disabled. Contact support for help."
            message.contains("too many", ignoreCase = true) ->
                "Too many attempts. Please wait a moment and try again."
            message.contains("email address is already", ignoreCase = true) ||
                message.contains("EMAIL_EXISTS", ignoreCase = true) ->
                "An account already exists for this email."
            message.contains("Profile not found", ignoreCase = true) ->
                "Your sign-in worked, but your profile could not be found."
            message.contains("permission", ignoreCase = true) ->
                "We could not access your profile. Please check your account permissions."
            else -> fallback
        }
    }
}
