package com.medical.management.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.medical.management.data.repository.FirebaseMedicalRepository
import com.medical.management.domain.repository.MedicalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMedicalRepository(repository: FirebaseMedicalRepository): MedicalRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun firebaseApp(@ApplicationContext context: Context): FirebaseApp =
        FirebaseApp.initializeApp(context)
            ?: error("Firebase could not initialize. Confirm app/google-services.json matches com.medical.management.")

    @Provides @Singleton fun auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app)
    @Provides @Singleton fun firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app)
    @Provides @Singleton fun storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage.getInstance(app)
    @Provides @Singleton fun messaging(app: FirebaseApp): FirebaseMessaging {
        app.name
        return FirebaseMessaging.getInstance()
    }
}
