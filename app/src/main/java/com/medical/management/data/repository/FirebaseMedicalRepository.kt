package com.medical.management.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.medical.management.data.model.Appointment
import com.medical.management.data.model.AppointmentStatus
import com.medical.management.data.model.AuthSession
import com.medical.management.data.model.AuthForm
import com.medical.management.data.model.Bill
import com.medical.management.data.model.DashboardStats
import com.medical.management.data.model.MedicalNotification
import com.medical.management.data.model.MedicalUser
import com.medical.management.domain.repository.MedicalRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMedicalRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val messaging: FirebaseMessaging
) : MedicalRepository {
    override val authSession: Flow<AuthSession> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(AuthSession(loading = false, authenticated = false))
                return@AuthStateListener
            }

            trySend(AuthSession(loading = true, authenticated = true))
            launch {
                val profile = loadUserProfile(firebaseUser.uid)
                if (profile != null) {
                    trySend(AuthSession(loading = false, authenticated = true, user = profile))
                } else {
                    trySend(
                        AuthSession(
                            loading = false,
                            authenticated = true,
                            message = "We signed you in, but could not load your profile. Check your connection and try again."
                        )
                    )
                }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUser: Flow<MedicalUser?> = authSession.map { it.user }

    override suspend fun login(email: String, password: String): Result<MedicalUser> = runCatching {
        val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Unable to sign in")
        loadUserProfile(user.uid) ?: MedicalUser(
            uid = user.uid,
            email = user.email.orEmpty(),
            name = user.displayName.orEmpty()
        )
    }

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
            bloodGroup = form.bloodGroup.trim(),
            department = form.department.trim(),
            address = form.address.trim(),
            specialization = form.specialization.trim(),
            qualification = form.qualification.trim(),
            experience = form.experience.trim(),
            hospitalClinic = form.hospitalClinic.trim()
        )
        try {
            firestore.collection(USERS).document(user.uid).set(user).await()
            if (token.isNotBlank()) {
                firestore.collection(USERS).document(user.uid).update("fcmToken", token).await()
            }
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            throw Exception("Failed to save user profile: ${e.message}")
        }
        user
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching { auth.sendPasswordResetEmail(email.trim()).await() }

    override suspend fun logout() {
        auth.signOut()
    }

    override fun doctors(search: String, specialization: String): Flow<List<MedicalUser>> =
        usersByRole("DOCTOR") { users ->
            users.filter {
                it.name.contains(search, true) &&
                    (specialization.isBlank() || it.specialization.contains(specialization, true))
            }
        }

    override fun patients(search: String): Flow<List<MedicalUser>> =
        usersByRole("PATIENT") { users -> users.filter { it.name.contains(search, true) || it.phone.contains(search) } }

    override fun appointmentsForPatient(patientId: String, query: String): Flow<List<Appointment>> =
        collectionFlow(APPOINTMENTS, Appointment::class.java) { ref ->
            ref.whereEqualTo("patientId", patientId).orderBy("createdAt", Query.Direction.DESCENDING)
        }.mapLocal { rows ->
            rows.filter { it.doctorName.contains(query, true) || it.status.contains(query, true) }
        }

    override fun appointmentsForDoctor(doctorId: String, status: String?, today: String?, query: String): Flow<List<Appointment>> =
        collectionFlow(APPOINTMENTS, Appointment::class.java) { ref ->
            var queryRef: Query = ref.whereEqualTo("doctorId", doctorId).orderBy("createdAt", Query.Direction.DESCENDING)
            if (status != null) queryRef = queryRef.whereEqualTo("status", status)
            if (today != null) queryRef = queryRef.whereEqualTo("appointmentDate", today)
            queryRef
        }.mapLocal { rows ->
            rows.filter { it.patientName.contains(query, true) || it.reason.contains(query, true) }
        }

    override fun treatmentsForPatient(patientId: String): Flow<List<com.medical.management.data.model.Treatment>> =
        collectionFlow(TREATMENTS, com.medical.management.data.model.Treatment::class.java) { ref ->
            ref.whereEqualTo("patientId", patientId).orderBy("createdAt", Query.Direction.DESCENDING)
        }

    override fun billsForPatient(patientId: String, query: String): Flow<List<Bill>> =
        collectionFlow(BILLS, Bill::class.java) { ref ->
            ref.whereEqualTo("patientId", patientId).orderBy("createdAt", Query.Direction.DESCENDING)
        }.mapLocal { bills ->
            bills.filter { it.description.contains(query, true) || it.generatedDate.contains(query, true) }
        }

    override suspend fun bookAppointment(appointment: Appointment): Result<Unit> = runCatching {
        val id = firestore.collection(APPOINTMENTS).document().id
        val item = appointment.copy(appointmentId = id, status = AppointmentStatus.PENDING.name)
        firestore.collection(APPOINTMENTS).document(id).set(item).await()
        notify(item.doctorId, "Appointment booked", "${item.patientName} requested ${item.appointmentDate} at ${item.appointmentTime}")
    }

    override suspend fun updateAppointmentStatus(appointmentId: String, status: String): Result<Unit> = runCatching {
        val document = firestore.collection(APPOINTMENTS).document(appointmentId)
        val appointment = document.get().await().toObject(Appointment::class.java)
        document.update("status", status).await()
        appointment?.let { notify(it.patientId, "Appointment $status", "Your appointment with ${it.doctorName} is now $status") }
    }

    override suspend fun saveTreatment(treatment: com.medical.management.data.model.Treatment): Result<Unit> = runCatching {
        val id = treatment.treatmentId.ifBlank { firestore.collection(TREATMENTS).document().id }
        firestore.collection(TREATMENTS).document(id).set(treatment.copy(treatmentId = id)).await()
        notify(treatment.patientId, "Treatment updated", "Your treatment record was updated by ${treatment.doctorName}")
    }

    override suspend fun saveBill(bill: Bill): Result<Unit> = runCatching {
        val id = bill.billId.ifBlank { firestore.collection(BILLS).document().id }
        val amount = bill.consultationFee + bill.medicineFee + bill.testsFee
        firestore.collection(BILLS).document(id).set(bill.copy(billId = id, amount = amount)).await()
        notify(bill.patientId, "Bill generated", "A bill of $amount was generated by ${bill.doctorName}")
    }

    override suspend fun updateProfile(user: MedicalUser): Result<Unit> =
        runCatching { firestore.collection(USERS).document(user.uid).set(user).await() }

    override suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("profile_images/$uid.jpg")
        ref.putFile(uri).await()
        val url = ref.downloadUrl.await().toString()
        firestore.collection(USERS).document(uid).update("profileImage", url).await()
        url
    }

    override fun doctorStats(doctorId: String, today: String): Flow<DashboardStats> =
        callbackFlow {
            val listener = firestore.collection(APPOINTMENTS).whereEqualTo("doctorId", doctorId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(DashboardStats())
                        return@addSnapshotListener
                    }
                    val appointments = snapshot?.toObjects(Appointment::class.java).orEmpty()
                    val patients = appointments.map { it.patientId }.toSet().size
                    trySend(
                        DashboardStats(
                            totalPatients = patients,
                            totalAppointments = appointments.size,
                            pendingAppointments = appointments.count { it.status == AppointmentStatus.PENDING.name },
                            todaysAppointments = appointments.count { it.appointmentDate == today }
                        )
                    )
                }
            awaitClose { listener.remove() }
        }

    private fun usersByRole(role: String, filter: (List<MedicalUser>) -> List<MedicalUser>): Flow<List<MedicalUser>> =
        collectionFlow(USERS, MedicalUser::class.java) { ref -> ref.whereEqualTo("role", role).orderBy("name") }
            .mapLocal(filter)

    private fun <T : Any> collectionFlow(
        collection: String,
        clazz: Class<T>,
        query: (com.google.firebase.firestore.CollectionReference) -> Query
    ): Flow<List<T>> = callbackFlow {
        val listener = query(firestore.collection(collection)).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
            } else {
                trySend(snapshot?.toObjects(clazz).orEmpty())
            }
        }
        awaitClose { listener.remove() }
    }

    private fun <T> Flow<List<T>>.mapLocal(transform: (List<T>) -> List<T>): Flow<List<T>> =
        map { transform(it) }

    private suspend fun loadUserProfile(uid: String): MedicalUser? {
        repeat(PROFILE_LOAD_ATTEMPTS) { attempt ->
            val snapshot = runCatching { firestore.collection(USERS).document(uid).get().await() }.getOrNull()
            val user = snapshot?.toObject(MedicalUser::class.java)
            if (user != null) return user
            if (attempt < PROFILE_LOAD_ATTEMPTS - 1) delay(PROFILE_LOAD_RETRY_MS)
        }
        return null
    }

    private suspend fun notify(userId: String, title: String, message: String) {
        val id = firestore.collection(NOTIFICATIONS).document().id
        firestore.collection(NOTIFICATIONS).document(id).set(
            MedicalNotification(notificationId = id, userId = userId, title = title, message = message)
        ).await()
    }

    private companion object {
        const val USERS = "users"
        const val APPOINTMENTS = "appointments"
        const val TREATMENTS = "treatments"
        const val BILLS = "bills"
        const val NOTIFICATIONS = "notifications"
        const val PROFILE_LOAD_ATTEMPTS = 4
        const val PROFILE_LOAD_RETRY_MS = 350L
    }
}
