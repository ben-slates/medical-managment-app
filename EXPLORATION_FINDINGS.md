# Medical Management System - Exploration Findings

## 1. Authentication & Login Screens

### File: [app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt](app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt)

**Login Screen** (Lines 48-62):
```kotlin
@Composable
fun LoginScreen(onRegister: () -> Unit, onForgot: () -> Unit, onSignedIn: (String) -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    state.data?.let { onSignedIn(it.role) }
    AuthFrame("Login") {
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { vm.login(email, password) }, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) { Text("Sign in") }
        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        OutlinedButton(onClick = onForgot, modifier = Modifier.fillMaxWidth()) { Text("Forgot password") }
        Message(state.message)
    }
}
```

**Key Input Fields:**
- Email field (plain text, single line)
- Password field (with `PasswordVisualTransformation()`)

---

## 2. Patient Account Creation/Registration Screen

### File: [app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt](app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt)

**RegisterScreen** (Lines 64-88):
```kotlin
@Composable
fun RegisterScreen(onLogin: () -> Unit, onSignedIn: (String) -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var form by remember { mutableStateOf(AuthForm()) }
    val state by vm.state.collectAsState()
    state.data?.let { onSignedIn(it.role) }
    AuthFrame("Register") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(form.role == UserRole.PATIENT, onClick = { form = form.copy(role = UserRole.PATIENT) }, label = { Text("Patient") })
            FilterChip(form.role == UserRole.DOCTOR, onClick = { form = form.copy(role = UserRole.DOCTOR) }, label = { Text("Doctor") })
        }
        Field("Full name", form.fullName) { form = form.copy(fullName = it) }
        Field("Email", form.email) { form = form.copy(email = it) }
        Field("Phone", form.phone) { form = form.copy(phone = it) }
        if (form.role == UserRole.PATIENT) {
            Field("Gender", form.gender) { form = form.copy(gender = it) }
            Field("Date of birth", form.dateOfBirth) { form = form.copy(dateOfBirth = it) }
            Field("Address", form.address) { form = form.copy(address = it) }
        } else {
            Field("Specialization", form.specialization) { form = form.copy(specialization = it) }
            Field("Qualification", form.qualification) { form = form.copy(qualification = it) }
            Field("Experience", form.experience) { form = form.copy(experience = it) }
            Field("Hospital/Clinic", form.hospitalClinic) { form = form.copy(hospitalClinic = it) }
        }
        OutlinedTextField(form.password, { form = form.copy(password = it) }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { vm.register(form) }, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) { Text("Create account") }
        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Back to login") }
        Message(state.message)
    }
}
```

**Patient-Specific Fields:**
- Gender (text input)
- Date of birth (text input - YYYY-MM-DD format expected)
- Address (text input)

**Doctor-Specific Fields:**
- Specialization
- Qualification
- Experience
- Hospital/Clinic

---

## 3. Password Reset Screen

### File: [app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt](app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt)

**ForgotPasswordScreen** (Lines 94-101):
```kotlin
@Composable
fun ForgotPasswordScreen(onLogin: () -> Unit, vm: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()
    AuthFrame("Reset Password") {
        Field("Email", email) { email = it }
        Button(onClick = { vm.reset(email) }, modifier = Modifier.fillMaxWidth()) { Text("Send reset email") }
        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Back to login") }
        Message(state.message)
    }
}
```

---

## 4. Input Field Helper Function

### File: [app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt](app/src/main/java/com/medical/management/presentation/auth/AuthScreens.kt)

**Field Composable** (Lines 118-119):
```kotlin
@Composable private fun Field(label: String, value: String, onChange: (String) -> Unit) =
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true)
```

---

## 5. Input Validation Logic

### File: [app/src/main/java/com/medical/management/utils/Validators.kt](app/src/main/java/com/medical/management/utils/Validators.kt)

**Login Validation:**
```kotlin
fun login(email: String, password: String): String {
    if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
    if (password.length < 8) return "Password must be at least 8 characters"
    return ""
}
```

**Registration Validation:**
```kotlin
fun registration(form: AuthForm): String {
    if (form.fullName.isBlank()) return "Full name is required"
    if (!Patterns.EMAIL_ADDRESS.matcher(form.email.trim()).matches()) return "Enter a valid email address"
    if (form.phone.length < 10) return "Enter a valid phone number"
    if (form.password.length < 8) return "Password must be at least 8 characters"
    return when (form.role) {
        UserRole.PATIENT -> when {
            form.gender.isBlank() -> "Gender is required"
            form.dateOfBirth.isBlank() -> "Date of birth is required"
            form.address.isBlank() -> "Address is required"
            else -> ""
        }
        UserRole.DOCTOR -> when {
            form.specialization.isBlank() -> "Specialization is required"
            form.qualification.isBlank() -> "Qualification is required"
            form.experience.isBlank() -> "Experience is required"
            form.hospitalClinic.isBlank() -> "Hospital or clinic is required"
            else -> ""
        }
    }
}
```

**Key Validations:**
- Email: Uses `Patterns.EMAIL_ADDRESS` regex matcher
- Password: Minimum 8 characters
- Phone: Minimum 10 characters
- Date of Birth: Required field (no specific format validation)

---

## 6. Data Model for Authentication

### File: [app/src/main/java/com/medical/management/data/model/Models.kt](app/src/main/java/com/medical/management/data/model/Models.kt)

**AuthForm Data Class** (Lines 95-110):
```kotlin
data class AuthForm(
    val role: UserRole = UserRole.PATIENT,
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val specialization: String = "",
    val qualification: String = "",
    val experience: String = "",
    val hospitalClinic: String = "",
    val password: String = ""
)
```

**MedicalUser Data Class** (Lines 5-20):
```kotlin
data class MedicalUser(
    val uid: String = "",
    val role: String = UserRole.PATIENT.name,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val specialization: String = "",
    val qualification: String = "",
    val experience: String = "",
    val hospitalClinic: String = "",
    val profileImage: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 7. Authentication Implementation

### File: [app/src/main/java/com/medical/management/data/repository/FirebaseMedicalRepository.kt](app/src/main/java/com/medical/management/data/repository/FirebaseMedicalRepository.kt)

**Login (Lines 47-52):**
```kotlin
override suspend fun login(email: String, password: String): Result<MedicalUser> = runCatching {
    val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
        ?: error("Unable to sign in")
    firestore.collection(USERS).document(user.uid).get().await()
        .toObject(MedicalUser::class.java) ?: error("Profile not found")
}
```

**Register (Lines 54-75):**
```kotlin
override suspend fun register(form: AuthForm): Result<MedicalUser> = runCatching {
    val firebaseUser = auth.createUserWithEmailAndPassword(form.email.trim(), form.password).await().user
        ?: error("Unable to create account")
    val token = runCatching { messaging.token.await() }.getOrDefault("")
    val user = MedicalUser(
        uid = firebaseUser.uid,
        role = form.role.name,
        name = form.fullName.trim(),
        email = form.email.trim(),
        phone = form.phone.trim(),
        gender = form.gender.trim(),
        dateOfBirth = form.dateOfBirth.trim(),
        address = form.address.trim(),
        specialization = form.specialization.trim(),
        qualification = form.qualification.trim(),
        experience = form.experience.trim(),
        hospitalClinic = form.hospitalClinic.trim()
    )
    firestore.collection(USERS).document(user.uid).set(user).await()
    if (token.isNotBlank()) {
        firestore.collection(USERS).document(user.uid).update("fcmToken", token).await()
    }
    user
}
```

**Error Handling:**
- `error("Unable to sign in")` - if Firebase user object is null
- `error("Profile not found")` - if Firestore document doesn't contain user data
- `error("Unable to create account")` - if account creation fails

---

## 8. Patient Dashboard & Profile Management

### File: [app/src/main/java/com/medical/management/presentation/patient/PatientScreens.kt](app/src/main/java/com/medical/management/presentation/patient/PatientScreens.kt)

**Patient Profile Display** (Lines 60-71):
```kotlin
@Composable
private fun PatientDashboard(vm: PatientViewModel) {
    val user by vm.user.collectAsState()
    val appointments by vm.appointments.collectAsState()
    val treatments by vm.treatments.collectAsState()
    val bills by vm.bills.collectAsState()
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Section("Profile") {
                InfoCard(user.name, "${user.email} | ${user.phone}", user.role) {
                    Text("${user.gender} | ${user.dateOfBirth}")
                    Text(user.address)
                }
            }
        }
        // ... appointments, treatments, bills ...
    }
}
```

**Profile Settings Screen** (Lines 165-173):
```kotlin
@Composable
private fun ProfileSettings(user: MedicalUser) {
    Section("Settings") {
        InfoCard(user.name, "${user.email} | ${user.phone}", user.role) {
            Text("Profile image upload is available through Firebase Storage in the repository layer.")
            Text("Use Firebase console security rules to enforce role-based access.")
        }
    }
}
```

---

## 9. Input Field Styling & Theme

### File: [app/src/main/java/com/medical/management/ui/theme/Theme.kt](app/src/main/java/com/medical/management/ui/theme/Theme.kt)

**Theme Configuration:**
```kotlin
private val Scheme = lightColorScheme(
    primary = Color(0xFF0F766E),      // Teal
    secondary = Color(0xFF2563EB),    // Blue
    tertiary = Color(0xFF7C3AED),     // Purple
    surface = Color(0xFFFAFAFA),      // Light gray
    background = Color(0xFFFFFFFF)    // White
)

@Composable
fun MedicalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
```

**UI Components Used:**
- `OutlinedTextField` - All input fields (uses Material 3 styling)
- `PasswordVisualTransformation()` - Password masking
- `FilterChip` - Role selection (Patient/Doctor)

**Key Observations:**
- All text fields use `OutlinedTextField` with default Material 3 styling
- No custom styling applied to input fields
- Single-line constraint on registration form fields
- No apparent custom text rendering that would cause mirroring

---

## 10. Configuration & Error Handling

### Error Messages in Repository (Lines 49-51, 56, 168-170, 195-197):
```kotlin
// Login/Profile loading errors
?: error("Unable to sign in")
?: error("Profile not found")
?: error("Unable to create account")

// Snapshot listening errors
if (error != null) {
    trySend(DashboardStats())  // Empty state on error
    return@addSnapshotListener
}

// Collection flow errors
if (error != null) {
    trySend(emptyList())       // Empty list on error
} else {
    trySend(snapshot?.toObjects(clazz).orEmpty())
}
```

**Configuration Files:**
- `app/google-services.json` - Firebase configuration (configuration_version: 1)
- `firestore.rules` - Firestore security rules (role-based access)
- `build.gradle.kts` - Build configuration

**No "configuration not found" errors** are explicitly handled in the codebase. Errors are propagated as generic messages.

---

## Summary of Key Components

| Component | File Location | Purpose |
|-----------|---------------|---------|
| Login Screen | AuthScreens.kt | Email/password authentication |
| Registration Screen | AuthScreens.kt | Patient/Doctor account creation |
| Password Reset | AuthScreens.kt | Email-based password recovery |
| Input Validation | Validators.kt | Email, password, phone, DOB validation |
| Firebase Auth | FirebaseMedicalRepository.kt | Authentication backend (Firebase) |
| Theme | Theme.kt | Material 3 design with custom colors |
| Patient Dashboard | PatientScreens.kt | Profile and appointment management |

---

## Notable Features

✅ **Jetpack Compose UI** - Fully declarative UI
✅ **Firebase Authentication** - Email/password with Firestore user profiles
✅ **Role-Based UI** - Different forms for patient vs. doctor registration
✅ **Input Validation** - Regex patterns for email, phone validation
✅ **State Management** - MutableStateFlow for reactive UI updates
✅ **Material 3 Design** - Consistent Material Design components
✅ **Hilt Dependency Injection** - ViewModels with DI

---

## Potential Issues to Investigate

1. **No explicit format validation for Date of Birth** - Currently just checks if field is non-empty
2. **Empty state handling** - Collection flows return empty lists on Firestore errors (silent failures)
3. **Password reset email delivery** - No explicit error handling or success confirmation UI
4. **Profile image upload** - Mentioned but not fully implemented in screens
5. **Input field rendering** - No apparent mirroring issues in the code, but verify visual behavior on devices
