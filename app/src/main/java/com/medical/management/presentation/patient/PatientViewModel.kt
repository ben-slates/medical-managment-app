package com.medical.management.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.management.data.model.Appointment
import com.medical.management.data.model.Bill
import com.medical.management.data.model.MedicalUser
import com.medical.management.data.model.Treatment
import com.medical.management.domain.repository.MedicalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PatientViewModel @Inject constructor(
    private val repository: MedicalRepository
) : ViewModel() {
    private val _user = MutableStateFlow(MedicalUser())
    val user: StateFlow<MedicalUser> = _user.asStateFlow()
    val doctorSearch = MutableStateFlow("")
    val billSearch = MutableStateFlow("")
    val message = MutableStateFlow("")

    val doctors = doctorSearch.flatMapLatest { repository.doctors(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val appointments = _user.flatMapLatest { repository.appointmentsForPatient(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val treatments = _user.flatMapLatest { repository.treatmentsForPatient(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bills = _user.flatMapLatest { user -> billSearch.flatMapLatest { repository.billsForPatient(user.uid, it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setUser(user: MedicalUser) {
        _user.value = user
    }

    fun book(doctor: MedicalUser, date: String, time: String, reason: String) = viewModelScope.launch {
        val current = _user.value
        if (date.isBlank() || time.isBlank()) {
            message.value = "Date and time are required"
            return@launch
        }
        repository.bookAppointment(
            Appointment(
                patientId = current.uid,
                doctorId = doctor.uid,
                patientName = current.name,
                doctorName = doctor.name,
                appointmentDate = date,
                appointmentTime = time,
                reason = reason
            )
        ).fold(
            onSuccess = { message.value = "Appointment requested" },
            onFailure = { message.value = it.message ?: "Unable to book appointment" }
        )
    }

    fun cancel(appointmentId: String) = viewModelScope.launch {
        repository.updateAppointmentStatus(appointmentId, "CANCELLED")
    }
}
