package com.medical.management.domain.repository

import android.net.Uri
import com.medical.management.data.model.Appointment
import com.medical.management.data.model.AuthSession
import com.medical.management.data.model.AuthForm
import com.medical.management.data.model.Bill
import com.medical.management.data.model.DashboardStats
import com.medical.management.data.model.MedicalUser
import com.medical.management.data.model.Treatment
import kotlinx.coroutines.flow.Flow

interface MedicalRepository {
    val authSession: Flow<AuthSession>
    val currentUser: Flow<MedicalUser?>
    suspend fun login(email: String, password: String): Result<MedicalUser>
    suspend fun register(form: AuthForm): Result<MedicalUser>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun logout()
    fun doctors(search: String = "", specialization: String = ""): Flow<List<MedicalUser>>
    fun patients(search: String = ""): Flow<List<MedicalUser>>
    fun appointmentsForPatient(patientId: String, query: String = ""): Flow<List<Appointment>>
    fun appointmentsForDoctor(doctorId: String, status: String? = null, today: String? = null, query: String = ""): Flow<List<Appointment>>
    fun treatmentsForPatient(patientId: String): Flow<List<Treatment>>
    fun billsForPatient(patientId: String, query: String = ""): Flow<List<Bill>>
    suspend fun bookAppointment(appointment: Appointment): Result<Unit>
    suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit>
    suspend fun saveTreatment(treatment: Treatment): Result<Unit>
    suspend fun saveBill(bill: Bill): Result<Unit>
    suspend fun updateProfile(user: MedicalUser): Result<Unit>
    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String>
    fun doctorStats(doctorId: String, today: String): Flow<DashboardStats>
}
