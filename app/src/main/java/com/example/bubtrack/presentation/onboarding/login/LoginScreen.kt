package com.example.bubtrack.presentation.onboarding.login

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.common.PasswordTextField
import com.example.bubtrack.presentation.navigation.AppRoute
import com.example.bubtrack.presentation.navigation.ForgotPasswordRoute
import com.example.bubtrack.presentation.navigation.RegisterRoute
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    navigate: (AppRoute) -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uiState by loginViewModel.loginState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .requestProfile()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                Log.d("LoginScreen", "Selected account: ${it.email}, ${it.displayName}")
                loginViewModel.loginWithGoogle(it)
            } ?: run {
                Toast.makeText(context, "Google Sign-In failed: No account returned", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Log.e("LoginScreen", "Google Sign-In failed: StatusCode=${e.statusCode}, Message=${e.message}", e)
            Toast.makeText(context, "Google Sign-In failed: ${e.message} (Code: ${e.statusCode})", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Selamat Datang, Moms",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                "Masuk untuk terus mendampingi si kecil.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            CommonTextField(
                value = email,
                placeholder = "Email",
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            PasswordTextField(
                value = password,
                placeholder = "Password",
                onValueChange = { password = it }
            )
            TextButton(
                onClick = {
                    navController.navigate(ForgotPasswordRoute)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "Lupa Sandi?",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AppPurple
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Lengkapi email dan password!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        loginViewModel.loginWithEmailPassword(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppPurple
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(width = 0.dp, color = Color.Transparent)
            ) {
                Text(
                    "Masuk",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
            Text(
                "Atau",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            googleSignInClient.signOut().await()
                            Log.d("LoginScreen", "Signed out from Google Sign-In")
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Sign-out failed: ${e.message}", e)
                        }
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(width = 1.dp, color = AppPurple)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_google),
                        contentDescription = "google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Masuk dengan Google",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppPurple
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Belum punya akun?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    "Daftar",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppPurple,
                    modifier = Modifier.clickable {
                        navController.navigate(RegisterRoute)
                    }
                )
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppPurple
            )
        }
        if (uiState.errorMessage != null) {
            LaunchedEffect(uiState.errorMessage) {
                Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            }
        }
        LaunchedEffect(uiState.navDestination) {
            uiState.navDestination?.let {
                navigate(it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BubTrackTheme {
        LoginScreen(
            navController = rememberNavController(),
            navigate = {}
        )
    }
}