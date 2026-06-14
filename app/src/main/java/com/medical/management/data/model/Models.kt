package com.medical.management.data.model

enum class UserRole { PATIENT, DOCTOR }
enum class AppointmentStatus { PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED }

data class MedicalUser(
    val uid: String = "",
    val role: String = UserRole.PATIENT.name,
    val name: String = "",
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
    val profileImage: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Appointment(
    val appointmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val reason: String = "",
    val status: String = AppointmentStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
)

data class Treatment(
    val treatmentId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val disease: String = "",
    val diagnosis: String = "",
    val prescription: String = "",
    val doctorNotes: String = "",
    val progress: String = "",
    val followUpDate: String = "",
    val treatmentDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Bill(
    val billId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val patientName: String = "",
    val doctorName: String = "",
    val consultationFee: Double = 0.0,
    val medicineFee: Double = 0.0,
    val testsFee: Double = 0.0,
    val services: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val generatedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Prescription(
    val prescriptionId: String = "",
    val patientId: String = "",
    val doctorId: String = "",
    val treatmentId: String = "",
    val medicines: String = "",
    val instructions: String = "",
    val generatedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MedicalNotification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthForm(
    val role: UserRole = UserRole.PATIENT,
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
)

data class DashboardStats(
    val totalPatients: Int = 0,
    val totalAppointments: Int = 0,
    val pendingAppointments: Int = 0,
    val todaysAppointments: Int = 0
)

data class UiState<T>(
    val loading: Boolean = false,
    val data: T? = null,
    val message: String = ""
)

data class AuthSession(
    val loading: Boolean = true,
    val authenticated: Boolean = false,
    val user: MedicalUser? = null,
    val message: String = ""
)
