package com.medical.management.presentation.doctor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medical.management.data.model.Appointment
import com.medical.management.data.model.AppointmentStatus
import com.medical.management.data.model.MedicalUser
import com.medical.management.presentation.shared.InfoCard
import com.medical.management.presentation.shared.MedicalScaffold
import com.medical.management.presentation.shared.MetricCard
import com.medical.management.presentation.shared.NavItem
import com.medical.management.presentation.shared.SearchBox
import com.medical.management.presentation.shared.Section

@Composable
fun DoctorHome(user: MedicalUser, onLogout: () -> Unit, vm: DoctorViewModel = hiltViewModel()) {
    LaunchedEffect(user.uid) { vm.setUser(user) }
    var tab by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavItem("Home", Icons.Default.Dashboard),
        NavItem("Pending", Icons.Default.PendingActions),
        NavItem("Today", Icons.Default.CalendarToday),
        NavItem("Patients", Icons.Default.Groups),
        NavItem("Care", Icons.Default.MedicalServices),
        NavItem("Bill", Icons.Default.AccountBalanceWallet),
        NavItem("Profile", Icons.Default.Person)
    )
    MedicalScaffold("Doctor Portal", onLogout, items, tab, { tab = it }) { modifier ->
        Column(modifier.fillMaxSize()) {
            when (tab) {
                0 -> DoctorDashboard(vm)
                1 -> AppointmentList(vm, pending = true)
                2 -> AppointmentList(vm, pending = false)
                3 -> PatientDirectory(vm)
                4 -> TreatmentEditor(vm)
                5 -> BillEditor(vm)
                else -> DoctorProfile(user)
            }
        }
    }
}

@Composable
private fun DoctorDashboard(vm: DoctorViewModel) {
    val user by vm.user.collectAsState()
    val stats by vm.stats.collectAsState()
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Section("Profile") {
                InfoCard(user.name, "${user.specialization} | ${user.hospitalClinic}", user.experience) {
                    Text("${user.department} | ${user.qualification} | ${user.phone}")
                }
            }
        }
        item {
            Section("Analytics") {
                MetricCard("Total Patients", stats.totalPatients.toString())
                MetricCard("Total Appointments", stats.totalAppointments.toString())
                MetricCard("Pending Appointments", stats.pendingAppointments.toString())
                MetricCard("Today's Appointments", stats.todaysAppointments.toString())
            }
        }
    }
}

@Composable
private fun AppointmentList(vm: DoctorViewModel, pending: Boolean) {
    val pendingAppointments by vm.pending.collectAsState()
    val todaysAppointments by vm.today.collectAsState()
    val appointments = if (pending) pendingAppointments else todaysAppointments
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(appointments) { AppointmentCard(it, vm) }
    }
}

@Composable
private fun AppointmentCard(appointment: Appointment, vm: DoctorViewModel) {
    InfoCard(appointment.patientName, "${appointment.appointmentDate} ${appointment.appointmentTime} | ${appointment.reason}", appointment.status) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.status(appointment, AppointmentStatus.APPROVED) }) { Text("Approve") }
            Button(onClick = { vm.status(appointment, AppointmentStatus.REJECTED) }) { Text("Reject") }
            Button(onClick = { vm.status(appointment, AppointmentStatus.COMPLETED) }) { Text("Complete") }
        }
    }
}

@Composable
private fun PatientDirectory(vm: DoctorViewModel) {
    val patients by vm.patients.collectAsState()
    val search by vm.patientSearch.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchBox(search, { vm.patientSearch.value = it }, "Search patients") }
        items(patients) { patient ->
            InfoCard(patient.name, "${patient.phone} | ${patient.email}", patient.gender) {
                Text("${patient.dateOfBirth} | ${patient.address}")
            }
        }
    }
}

@Composable
private fun TreatmentEditor(vm: DoctorViewModel) {
    val patients by vm.patients.collectAsState()
    val message by vm.message.collectAsState()
    var selected by remember { mutableStateOf<MedicalUser?>(null) }
    var disease by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var progress by remember { mutableStateOf("") }
    var followUp by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { PatientChooser(patients, selected) { selected = it } }
        item {
            SmallField("Disease", disease) { disease = it }
            SmallField("Diagnosis", diagnosis) { diagnosis = it }
            SmallField("Prescription", prescription) { prescription = it }
            SmallField("Progress notes", progress) { progress = it }
            SmallField("Follow-up date", followUp) { followUp = it }
            Button(
                onClick = { selected?.let { vm.saveTreatment(it, disease, diagnosis, prescription, progress, followUp) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save treatment") }
            if (message.isNotBlank()) Text(message)
        }
    }
}

@Composable
private fun BillEditor(vm: DoctorViewModel) {
    val patients by vm.patients.collectAsState()
    val message by vm.message.collectAsState()
    var selected by remember { mutableStateOf<MedicalUser?>(null) }
    var services by remember { mutableStateOf("") }
    var consultation by remember { mutableStateOf("") }
    var medicine by remember { mutableStateOf("") }
    var tests by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { PatientChooser(patients, selected) { selected = it } }
        item {
            SmallField("Services", services) { services = it }
            SmallField("Consultation fee", consultation, KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)) { consultation = it }
            SmallField("Medicine fee", medicine, KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)) { medicine = it }
            SmallField("Tests fee", tests, KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)) { tests = it }
            SmallField("Description", description) { description = it }
            Button(
                onClick = {
                    selected?.let { vm.saveBill(it, services, consultation.toDoubleOrNull() ?: 0.0, medicine.toDoubleOrNull() ?: 0.0, tests.toDoubleOrNull() ?: 0.0, description) }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate bill") }
            if (message.isNotBlank()) Text(message)
        }
    }
}

@Composable
private fun PatientChooser(patients: List<MedicalUser>, selected: MedicalUser?, onSelect: (MedicalUser) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Patient: ${selected?.name ?: "Select"}")
        patients.take(6).forEach { patient ->
            Button(onClick = { onSelect(patient) }, modifier = Modifier.fillMaxWidth()) { Text(patient.name) }
        }
    }
}

@Composable
private fun DoctorProfile(user: MedicalUser) {
    Section("Doctor Profile") {
        InfoCard(user.name, "${user.email} | ${user.phone}", user.role) {
            Text("${user.department} | ${user.specialization} | ${user.qualification}")
            Text("${user.experience} | ${user.hospitalClinic}")
        }
    }
}

@Composable
private fun SmallField(
    label: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit
) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) }, keyboardOptions = keyboardOptions)
}
