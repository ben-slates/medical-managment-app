package com.medical.management.utils

import android.util.Patterns
import com.medical.management.data.model.AuthForm
import com.medical.management.data.model.UserRole

object Validators {
    data class RegistrationErrors(
        val fullName: String = "",
        val email: String = "",
        val phone: String = "",
        val gender: String = "",
        val dateOfBirth: String = "",
        val bloodGroup: String = "",
        val department: String = "",
        val address: String = "",
        val specialization: String = "",
        val qualification: String = "",
        val experience: String = "",
        val hospitalClinic: String = "",
        val password: String = ""
    ) {
        val hasErrors: Boolean
            get() = listOf(
                fullName,
                email,
                phone,
                gender,
                dateOfBirth,
                bloodGroup,
                department,
                address,
                specialization,
                qualification,
                experience,
                hospitalClinic,
                password
            ).any { it.isNotBlank() }

        fun firstMessage(): String = listOf(
            fullName,
            email,
            phone,
            gender,
            dateOfBirth,
            bloodGroup,
            department,
            address,
            specialization,
            qualification,
            experience,
            hospitalClinic,
            password
        ).firstOrNull { it.isNotBlank() }.orEmpty()
    }

    fun login(email: String, password: String): String {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) return "Enter a valid email address"
        if (password.length < 8) return "Password must be at least 8 characters"
        return ""
    }

    fun registration(form: AuthForm): String {
        return registrationErrors(form).firstMessage()
    }

    fun registrationErrors(form: AuthForm): RegistrationErrors {
        val phoneDigits = form.phone.filter(Char::isDigit)
        return RegistrationErrors(
            fullName = if (form.fullName.trim().length < 2) "Enter the patient's full name" else "",
            email = if (!Patterns.EMAIL_ADDRESS.matcher(form.email.trim()).matches()) "Enter a valid email address" else "",
            phone = if (phoneDigits.length !in 10..15) "Enter a 10 to 15 digit phone number" else "",
            gender = if (form.role == UserRole.PATIENT && form.gender.isBlank()) "Select gender" else "",
            dateOfBirth = if (form.role == UserRole.PATIENT && form.dateOfBirth.isBlank()) "Select date of birth" else "",
            bloodGroup = if (form.role == UserRole.PATIENT && form.bloodGroup.isBlank()) "Select blood group" else "",
            department = if (form.role == UserRole.PATIENT && form.department.isBlank()) "Select department" else "",
            address = if (form.role == UserRole.PATIENT && form.address.isBlank()) "Address is required" else "",
            specialization = if (form.role == UserRole.DOCTOR && form.specialization.isBlank()) "Specialization is required" else "",
            qualification = if (form.role == UserRole.DOCTOR && form.qualification.isBlank()) "Qualification is required" else "",
            experience = if (form.role == UserRole.DOCTOR && form.experience.isBlank()) "Experience is required" else "",
            hospitalClinic = if (form.role == UserRole.DOCTOR && form.hospitalClinic.isBlank()) "Hospital or clinic is required" else "",
            password = if (form.password.length < 8) "Password must be at least 8 characters" else ""
        )
    }
}
