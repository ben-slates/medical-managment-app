# Medical Management System

Medical Management System is a Kotlin Android app for patient and doctor workflows. It uses Jetpack Compose, Material 3, MVVM, repository-based data access, Hilt dependency injection, Firebase Authentication, Firestore, Firebase Storage, Firebase Cloud Messaging, Coroutines, Flow, Android SDK 35, minimum SDK 26, and Java 21.

## Features

- Patient and doctor registration with role-specific fields.
- Login, registration, forgot password, and splash screens.
- Patient dashboard with profile, upcoming appointments, recent treatments, and recent bills.
- Doctor dashboard with total patients, total appointments, pending appointments, and today's appointments.
- Appointment booking, cancellation, approval, rejection, and completion.
- Doctor treatment updates with disease, diagnosis, prescription, progress, notes, and follow-up date.
- Bill generation with consultation, medicine, test fees, services, total amount, searchable history, and PDF sharing.
- Prescription PDF generation and local storage.
- Search for doctors, patients, appointments, and bills.
- Firestore offline persistence enabled at app startup.
- Firestore role-based security rules in `firestore.rules`.

## Firebase Setup

1. Create a Firebase project in the Firebase console.
2. Add an Android app with package name `com.medical.management`.
3. Download `google-services.json`.
4. Place it at `app/google-services.json`.
5. Rebuild the project. The Google Services Gradle plugin is applied automatically when that file exists.

Firebase credentials are intentionally not committed or hardcoded.

## SHA-1 Generation

Run:

```bash
./gradlew signingReport
```

Copy the debug SHA-1 into Firebase Console > Project settings > Your Android app > SHA certificate fingerprints.

## Firestore Setup

Create the following collections as the app writes data:

- `users`
- `appointments`
- `treatments`
- `bills`
- `prescriptions`
- `notifications`

Deploy the rules:

```bash
firebase deploy --only firestore:rules
```

## FCM Setup

Firebase Cloud Messaging is included through the Firebase Android BoM. The app stores notification records in Firestore and includes `MedicalMessagingService` for incoming FCM payloads. Enable Cloud Messaging in Firebase Console and use a trusted backend or Cloud Functions to send push messages to stored user tokens.

## Running Locally

Requirements:

- OpenJDK 21
- Android SDK platform 35
- Android Studio with Kotlin and Compose support
- `app/google-services.json` for Firebase runtime access

Run a debug build:

```bash
./build.sh
```

## Debug Build

```bash
./build.sh
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Release Build

```bash
./build.sh --release
```

APK output:

```text
app/build/outputs/apk/release/app-release.apk
```

Configure release signing in Android Studio or CI before publishing a production release APK.

## APK Generation

The build script supports:

```bash
./build.sh
./build.sh --release
./build.sh clean
./build.sh test
```

It detects `JAVA_HOME`, `ANDROID_HOME`, verifies JDK 21, verifies Android SDK 35, runs the Gradle wrapper, prints colored logs, and exits with explicit error codes.

## Folder Structure

```text
app/src/main/java/com/medical/management/
  data/model
  data/repository
  di
  domain/repository
  domain/usecase
  navigation
  presentation/auth
  presentation/doctor
  presentation/patient
  presentation/shared
  ui/theme
  utils
```

## Troubleshooting

- If Firebase calls fail at runtime, confirm `app/google-services.json` exists and matches `com.medical.management`.
- If `./build.sh` reports Java 25 or another version, install OpenJDK 21 and export `JAVA_HOME` to that JDK.
- If SDK 35 is missing, install Android 15 / API 35 from Android Studio SDK Manager.
- If Firestore denies reads or writes, deploy `firestore.rules` and confirm the user role document exists in `users/{uid}`.
- If release builds fail, configure signing credentials before publishing.
