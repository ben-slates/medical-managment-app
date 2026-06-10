package com.medical.management.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.medical.management.presentation.auth.ForgotPasswordScreen
import com.medical.management.presentation.auth.LoginScreen
import com.medical.management.presentation.auth.RegisterScreen
import com.medical.management.presentation.auth.SplashScreen
import com.medical.management.presentation.doctor.DoctorHome
import com.medical.management.presentation.patient.PatientHome

@Composable
fun MedicalNavHost(
    navController: NavHostController = rememberNavController(),
    session: SessionViewModel = hiltViewModel()
) {
    val sessionState by session.state.collectAsState()
    val user = sessionState.user

    LaunchedEffect(sessionState.loading, user?.uid, user?.role) {
        if (sessionState.loading) return@LaunchedEffect
        when (user?.role) {
            "PATIENT" -> navController.navigate(Routes.PATIENT) { popUpTo(0) }
            "DOCTOR" -> navController.navigate(Routes.DOCTOR) { popUpTo(0) }
            null -> navController.navigate(Routes.LOGIN) { popUpTo(0) }
        }
    }

    NavHost(navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen() }
        composable(Routes.LOGIN) {
            LoginScreen(
                onRegister = { navController.navigate(Routes.REGISTER) },
                onForgot = { navController.navigate(Routes.FORGOT) },
                onSignedIn = { role -> navController.navigate(if (role == "DOCTOR") Routes.DOCTOR else Routes.PATIENT) { popUpTo(0) } }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onLogin = { navController.navigate(Routes.LOGIN) },
                onSignedIn = { role -> navController.navigate(if (role == "DOCTOR") Routes.DOCTOR else Routes.PATIENT) { popUpTo(0) } }
            )
        }
        composable(Routes.FORGOT) { ForgotPasswordScreen(onLogin = { navController.navigate(Routes.LOGIN) }) }
        composable(Routes.PATIENT) { user?.let { PatientHome(it, onLogout = session::logout) } ?: SplashScreen() }
        composable(Routes.DOCTOR) { user?.let { DoctorHome(it, onLogout = session::logout) } ?: SplashScreen() }
    }
}
