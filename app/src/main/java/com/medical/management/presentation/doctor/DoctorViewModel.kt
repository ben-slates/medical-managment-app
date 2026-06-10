package com.medical.management.presentation.doctor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.management.data.model.Appointment
import com.medical.management.data.model.AppointmentStatus
import com.medical.management.data.model.Bill
import com.medical.management.data.model.DashboardStats
import com.medical.management.data.model.MedicalUser
import com.medical.management.data.model.Treatment
import com.medical.management.domain.repository.MedicalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val repository: MedicalRepository
) : ViewModel() {
    private val _user = MutableStateFlow(MedicalUser())
    val user: StateFlow<MedicalUser> = _user.asStateFlow()
    val patientSearch = MutableStateFlow("")
    val message = MutableStateFlow("")

    val patients = patientSearch.flatMapLatest { repository.patients(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pending = _user.flatMapLatest { repository.appointmentsForDoctor(it.uid, AppointmentStatus.PENDING.name) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val today = _user.flatMapLatest { repository.appointmentsForDoctor(it.uid, today = LocalDate.now().toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats = _user.flatMapLatest { repository.doctorStats(it.uid, LocalDate.now().toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    fun setUser(user: MedicalUser) {
        _user.value = user
    }

    fun status(appointment: Appointment, status: AppointmentStatus) = viewModelScope.launch {
        repository.updateAppointmentStatus(appointment.appointmentId, status.name)
    }

    fun saveTreatment(patient: MedicalUser, disease: String, diagnosis: String, prescription: String, progress: String, followUp: String) =
        viewModelScope.launch {
            val doctor = _user.value
            if (patient.uid.isBlank() || disease.isBlank() || prescription.isBlank()) {
                message.value = "Patient, disease, and prescription are required"
                return@launch
            }
            repository.saveTreatment(
                Treatment(
                    patientId = patient.uid,
                    doctorId = doctor.uid,
                    patientName = patient.name,
                    doctorName = doctor.name,
                    disease = disease,
                    diagnosis = diagnosis,
                    prescription = prescription,
                    progress = progress,
                    followUpDate = followUp,
                    treatmentDate = LocalDate.now().toString()
                )
            ).fold(
                onSuccess = { message.value = "Treatment saved" },
                onFailure = { message.value = it.message ?: "Unable to save treatment" }
            )
        }

    fun saveBill(patient: MedicalUser, services: String, consultation: Double, medicine: Double, tests: Double, description: String) =
        viewModelScope.launch {
            val doctor = _user.value
            repository.saveBill(
                Bill(
                    patientId = patient.uid,
                    doctorId = doctor.uid,
                    patientName = patient.name,
                    doctorName = doctor.name,
                    consultationFee = consultation,
                    medicineFee = medicine,
                    testsFee = tests,
                    services = services,
                    description = description,
                    generatedDate = LocalDate.now().toString()
                )
            ).fold(
                onSuccess = { message.value = "Bill generated" },
                onFailure = { message.value = it.message ?: "Unable to generate bill" }
            )
        }
}
