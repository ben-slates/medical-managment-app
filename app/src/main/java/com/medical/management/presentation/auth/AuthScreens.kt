package com.medical.management.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medical.management.data.model.AuthForm
import com.medical.management.data.model.UserRole
import com.medical.management.presentation.shared.AppDatePickerField
import com.medical.management.presentation.shared.AppDropdown
import com.medical.management.presentation.shared.LoadingButton
import com.medical.management.presentation.shared.SecondaryAction
import com.medical.management.utils.Validators

private val genderOptions = listOf("Male", "Female", "Other")
private val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
private val departmentOptions = listOf("General Medicine", "Cardiology", "Neurology", "Orthopedics", "Pediatrics", "Dermatology", "Emergency")

@Composable
fun SplashScreen() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Medical Management System", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Secure care coordination for patients and doctors", color = MaterialTheme.colorScheme.onSurfaceVariant)
        CircularProgressIndicator(Modifier.padding(top = 24.dp))
    }
}

@Composable
fun LoginScreen(onRegister: () -> Unit, onForgot: () -> Unit, onSignedIn: (String) -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()
    LaunchedEffect(state.data?.uid) { state.data?.let { onSignedIn(it.role) } }
    AuthFrame("Welcome back") {
        OutlinedTextField(
            email,
            { email = it },
            Modifier.fillMaxWidth(),
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        PasswordField(password, { password = it }, showPassword, { showPassword = !showPassword })
        LoadingButton("Sign in", state.loading, { vm.login(email, password) })
        SecondaryAction("Create account", onRegister)
        SecondaryAction("Forgot password", onForgot)
        Message(state.message, state.data != null)
    }
}

@Composable
fun RegisterScreen(onLogin: () -> Unit, onSignedIn: (String) -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var form by remember { mutableStateOf(AuthForm()) }
    var showPassword by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()
    val errors = if (submitted) Validators.registrationErrors(form) else Validators.RegistrationErrors()
    LaunchedEffect(state.data?.uid) { state.data?.let { onSignedIn(it.role) } }
    AuthFrame("Patient Registration") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(form.role == UserRole.PATIENT, onClick = { form = form.copy(role = UserRole.PATIENT) }, label = { Text("Patient") })
            FilterChip(form.role == UserRole.DOCTOR, onClick = { form = form.copy(role = UserRole.DOCTOR) }, label = { Text("Doctor") })
        }
        ValidatedField("Full name", form.fullName, { form = form.copy(fullName = it) }, errors.fullName)
        ValidatedField(
            "Email",
            form.email,
            { form = form.copy(email = it) },
            errors.email,
            KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        ValidatedField(
            "Phone number",
            form.phone,
            { form = form.copy(phone = it.filter(Char::isDigit)) },
            errors.phone,
            KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        if (form.role == UserRole.PATIENT) {
            AppDropdown("Gender", form.gender, genderOptions, { form = form.copy(gender = it) }, error = errors.gender)
            AppDatePickerField("Date of birth", form.dateOfBirth, { form = form.copy(dateOfBirth = it) }, error = errors.dateOfBirth)
            AppDropdown("Blood group", form.bloodGroup, bloodGroupOptions, { form = form.copy(bloodGroup = it) }, error = errors.bloodGroup)
            AppDropdown("Department", form.department, departmentOptions, { form = form.copy(department = it) }, error = errors.department)
            ValidatedField("Address", form.address, { form = form.copy(address = it) }, errors.address, singleLine = false)
        } else {
            AppDropdown("Department", form.department, departmentOptions, { form = form.copy(department = it) })
            ValidatedField("Specialization", form.specialization, { form = form.copy(specialization = it) }, errors.specialization)
            ValidatedField("Qualification", form.qualification, { form = form.copy(qualification = it) }, errors.qualification)
            ValidatedField("Experience", form.experience, { form = form.copy(experience = it) }, errors.experience)
            ValidatedField("Hospital/Clinic", form.hospitalClinic, { form = form.copy(hospitalClinic = it) }, errors.hospitalClinic)
        }
        PasswordField(form.password, { form = form.copy(password = it) }, showPassword, { showPassword = !showPassword }, errors.password)
        LoadingButton(
            "Register",
            state.loading,
            {
                submitted = true
                if (!Validators.registrationErrors(form).hasErrors) vm.register(form)
            }
        )
        SecondaryAction("Back to login", onLogin)
        Message(state.message, state.data != null || state.message.contains("success", ignoreCase = true))
    }
}

@Composable
fun ForgotPasswordScreen(onLogin: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    AuthFrame("Reset password") {
        ValidatedField("Email", email, { email = it }, "", KeyboardOptions(keyboardType = KeyboardType.Email))
        LoadingButton("Send reset email", state.loading, { vm.reset(email) })
        SecondaryAction("Back to login", onLogin)
        Message(state.message, state.message.contains("sent", ignoreCase = true))
    }
}

@Composable
private fun AuthFrame(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Medical Management System", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                content()
            }
        }
    }
}

@Composable
private fun ValidatedField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value,
        onChange,
        Modifier.fillMaxWidth(),
        label = { Text(label) },
        isError = error.isNotBlank(),
        supportingText = { if (error.isNotBlank()) Text(error) },
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3
    )
}

@Composable
private fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    showPassword: Boolean,
    onToggle: () -> Unit,
    error: String = ""
) {
    OutlinedTextField(
        value,
        onChange,
        Modifier.fillMaxWidth(),
        label = { Text("Password") },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "Toggle password")
            }
        },
        isError = error.isNotBlank(),
        supportingText = { if (error.isNotBlank()) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true
    )
}

@Composable
private fun Message(message: String, success: Boolean) {
    if (message.isNotBlank()) {
        Text(
            message,
            color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
