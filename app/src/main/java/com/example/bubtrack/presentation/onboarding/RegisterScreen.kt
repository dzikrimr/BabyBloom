package com.example.bubtrack.presentation.onboarding

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.common.PasswordTextField
import com.example.bubtrack.presentation.navigation.CreateProfileRoute
import com.example.bubtrack.presentation.navigation.LoginRoute
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthException

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Buat Akun BabyGrow Mu!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "Mulai pantau perkembangan dan monitoring dengan AI.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Name",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = name,
            placeholder = "Name",
            onValueChange = { name = it }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Email",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = email,
            placeholder = "Email",
            onValueChange = { email = it }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = password,
            placeholder = "Password",
            onValueChange = { password = it }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Confirm Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = confirmPassword,
            placeholder = "Confirm Password",
            onValueChange = { confirmPassword = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "Lengkapi semua field!", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                } else {
                    coroutineScope.launch {
                        try {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        userId?.let {
                                            val userData = hashMapOf(
                                                "name" to name,
                                                "email" to email
                                            )
                                            firestore.collection("users").document(userId)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                                                    navController.navigate(CreateProfileRoute)
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Gagal menyimpan data pengguna!", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                                        val message = when (errorCode) {
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah digunakan!"
                                            "ERROR_INVALID_EMAIL" -> "Email tidak valid!"
                                            "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah!"
                                            else -> "Registrasi gagal: ${task.exception?.message}"
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
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
                "Daftar",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sudah punya akun?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                "Masuk",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppPurple,
                modifier = Modifier.clickable {
                    navController.navigate(LoginRoute)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    BubTrackTheme {
        RegisterScreen(navController = rememberNavController())
    }
}