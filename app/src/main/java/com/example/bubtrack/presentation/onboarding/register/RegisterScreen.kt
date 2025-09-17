package com.example.bubtrack.presentation.onboarding.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.common.PasswordTextField
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.presentation.navigation.CreateProfileRoute
import com.example.bubtrack.presentation.navigation.LoginRoute

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
            navController.navigate(CreateProfileRoute) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Buat Akun BabyBloom Mu!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Mulai pantau perkembangan dan monitoring dengan AI.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Name",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = state.name,
            placeholder = "Name",
            onValueChange = { viewModel.onNameChange(it) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Email",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = state.email,
            placeholder = "Email",
            onValueChange = { viewModel.onEmailChange(it) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = state.password,
            placeholder = "Password",
            onValueChange = { viewModel.onPasswordChange(it) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Confirm Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = state.confirmPassword,
            placeholder = "Confirm Password",
            onValueChange = { viewModel.onConfirmPasswordChange(it) }
        )
        Spacer(modifier = Modifier.height(32.dp))
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            OutlinedButton(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppPurple,
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(width = 0.dp, color = Color.Transparent)
            ) {
                Text(
                    text = "Daftar",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = "Masuk",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppPurple,
                modifier = Modifier.clickable {
                    navController.navigate(LoginRoute)
                }
            )
        }
    }

    if (state.isSuccess) {
        Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    BubTrackTheme {
        RegisterScreen(navController = rememberNavController())
    }
}