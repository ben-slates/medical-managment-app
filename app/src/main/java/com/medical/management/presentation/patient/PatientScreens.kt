package com.medical.management.presentation.patient

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medical.management.data.model.Bill
import com.medical.management.data.model.MedicalUser
import com.medical.management.data.model.Treatment
import com.medical.management.presentation.shared.AppDatePickerField
import com.medical.management.presentation.shared.AppDropdown
import com.medical.management.presentation.shared.DetailLine
import com.medical.management.presentation.shared.EmptyState
import com.medical.management.presentation.shared.InfoCard
import com.medical.management.presentation.shared.LoadingButton
import com.medical.management.presentation.shared.MedicalScaffold
import com.medical.management.presentation.shared.MetricCard
import com.medical.management.presentation.shared.NavItem
import com.medical.management.presentation.shared.SearchBox
import com.medical.management.presentation.shared.Section
import com.medical.management.utils.PdfGenerator

@Composable
fun PatientHome(user: MedicalUser, onLogout: () -> Unit, vm: PatientViewModel = hiltViewModel()) {
    LaunchedEffect(user.uid) { vm.setUser(user) }
    var tab by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavItem("Home", Icons.Default.Dashboard),
        NavItem("Doctors", Icons.Default.Groups),
        NavItem("Visits", Icons.Default.CalendarMonth),
        NavItem("Care", Icons.Default.Description),
        NavItem("Bills", Icons.Default.AccountBalanceWallet),
        NavItem("Profile", Icons.Default.Person)
    )
    MedicalScaffold("Patient Portal", onLogout, items, tab, { tab = it }) { modifier ->
        Column(modifier.fillMaxSize()) {
            when (tab) {
                0 -> PatientDashboard(vm)
                1 -> BookAppointment(vm)
                2 -> PatientAppointments(vm)
                3 -> TreatmentHistory(vm)
                4 -> BillHistory(vm)
                else -> ProfileSettings(user)
            }
        }
    }
}

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
                    DetailLine("Gender", user.gender)
                    DetailLine("Date of birth", user.dateOfBirth)
                    DetailLine("Blood group", user.bloodGroup)
                    DetailLine("Department", user.department)
                    DetailLine("Address", user.address)
                }
            }
        }
        item {
            Section("Overview") {
                MetricCard("Appointments", appointments.size.toString())
                MetricCard("Treatment records", treatments.size.toString())
                MetricCard("Bills", bills.size.toString())
            }
        }
        item {
            Section("Upcoming Appointments") {
                if (appointments.isEmpty()) EmptyState("No appointments yet", "Book a doctor visit from the Doctors tab.", Icons.Default.CalendarMonth)
                appointments.take(3).forEach { InfoCard(it.doctorName, "${it.appointmentDate} ${it.appointmentTime}", it.status) }
            }
        }
        item {
            Section("Recent Treatments") {
                if (treatments.isEmpty()) EmptyState("No treatment history", "Your doctor updates will appear here.")
                treatments.take(3).forEach { TreatmentCard(it) }
            }
        }
        item {
            Section("Recent Bills") {
                if (bills.isEmpty()) EmptyState("No bills generated", "Invoices and payment history will appear here.", Icons.Default.AccountBalanceWallet)
                bills.take(3).forEach { BillCard(it) }
            }
        }
    }
}

@Composable
private fun BookAppointment(vm: PatientViewModel) {
    val doctors by vm.doctors.collectAsState()
    val message by vm.message.collectAsState()
    val search by vm.doctorSearch.collectAsState()
    var selectedName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    val doctorOptions = doctors.map { "${it.name} - ${it.specialization}" }
    val selectedDoctor = doctors.firstOrNull { "${it.name} - ${it.specialization}" == selectedName }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SearchBox(search, { vm.doctorSearch.value = it }, "Search doctors or specialization") }
        item {
            if (doctors.isEmpty()) EmptyState("No doctors found", "Try a different name or specialization.", Icons.Default.Groups)
            AppDropdown("Doctor", selectedName, doctorOptions, { selectedName = it })
            AppDatePickerField("Appointment date", date, { date = it })
            SmallField("Time", time, { time = it }, KeyboardOptions(keyboardType = KeyboardType.Number))
            SmallField("Reason", reason, { reason = it }, singleLine = false)
            LoadingButton("Request appointment", false, { selectedDoctor?.let { vm.book(it, date, time, reason) } })
            if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
        }
        items(doctors) { doctor ->
            InfoCard(doctor.name, "${doctor.specialization} | ${doctor.hospitalClinic}", doctor.experience)
        }
    }
}

@Composable
private fun PatientAppointments(vm: PatientViewModel) {
    val appointments by vm.appointments.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (appointments.isEmpty()) {
            item { EmptyState("No appointments", "Requested and approved appointments will show here.", Icons.Default.CalendarMonth) }
        }
        items(appointments) {
            InfoCard(it.doctorName, "${it.appointmentDate} ${it.appointmentTime} | ${it.reason}", it.status) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.cancel(it.appointmentId) }) { Text("Cancel") }
                    OutlinedButton(onClick = { vm.cancel(it.appointmentId) }) { Text("Reschedule") }
                }
            }
        }
    }
}

@Composable
private fun TreatmentHistory(vm: PatientViewModel) {
    val treatments by vm.treatments.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (treatments.isEmpty()) {
            item { EmptyState("No treatment history", "Prescriptions and clinical notes will show here.") }
        }
        items(treatments) { TreatmentCard(it, allowPdf = true) }
    }
}

@Composable
private fun BillHistory(vm: PatientViewModel) {
    val bills by vm.bills.collectAsState()
    val search by vm.billSearch.collectAsState()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SearchBox(search, { vm.billSearch.value = it }, "Search bills") }
        if (bills.isEmpty()) {
            item { EmptyState("No bills", "Generated bills will show here.", Icons.Default.AccountBalanceWallet) }
        }
        items(bills) { BillCard(it, allowPdf = true) }
    }
}

@Composable
private fun TreatmentCard(treatment: Treatment, allowPdf: Boolean = false) {
    val context = LocalContext.current
    InfoCard(treatment.disease, "${treatment.doctorName} | ${treatment.treatmentDate}", treatment.progress) {
        DetailLine("Diagnosis", treatment.diagnosis)
        DetailLine("Prescription", treatment.prescription)
        DetailLine("Notes", treatment.doctorNotes)
        if (allowPdf) Button(onClick = { PdfGenerator.share(context, PdfGenerator.prescription(context, treatment)) }) { Text("Download PDF") }
    }
}

@Composable
private fun BillCard(bill: Bill, allowPdf: Boolean = false) {
    val context = LocalContext.current
    InfoCard("Bill ${bill.generatedDate}", bill.description, bill.amount.toString()) {
        DetailLine("Services", bill.services)
        DetailLine("Doctor", bill.doctorName)
        if (allowPdf) Button(onClick = { PdfGenerator.share(context, PdfGenerator.bill(context, bill)) }) { Text("Download PDF") }
    }
}

@Composable
private fun ProfileSettings(user: MedicalUser) {
    Section("Profile") {
        InfoCard(user.name, "${user.email} | ${user.phone}", user.role) {
            DetailLine("Gender", user.gender)
            DetailLine("Date of birth", user.dateOfBirth)
            DetailLine("Blood group", user.bloodGroup)
            DetailLine("Department", user.department)
            DetailLine("Address", user.address)
        }
    }
}

@Composable
private fun SmallField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value,
        onChange,
        Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3
    )
}
