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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.common.PasswordTextField
import com.example.bubtrack.presentation.navigation.LoginRoute
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {

    var name by remember {
        mutableStateOf("")
    }
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confirmPassword by remember {
        mutableStateOf("")
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
            "Buat Akun BabyGrow Mu!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "Mulai pantau perkembangan dan monitoring dengan AI.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier.height(32.dp))
        Text(
            "Email",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = name,
            placeholder = "Name",
            onValueChange = { name = it }
        )
        Spacer(modifier.height(12.dp))
        Text(
            "Email",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        CommonTextField(
            value = email,
            placeholder = "Email",
            onValueChange = { email = it }
        )
        Spacer(modifier.height(12.dp))
        Text(
            "Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = password,
            placeholder = "Password",
            onValueChange = { password = it }
        )
        Spacer(modifier.height(12.dp))
        Text(
            "Confirm Password",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = modifier
                .align(Alignment.Start)
                .padding(bottom = 5.dp)
        )
        PasswordTextField(
            value = confirmPassword,
            placeholder = "Password",
            onValueChange = { confirmPassword = it }
        )
        Spacer(modifier.height(32.dp))
        OutlinedButton(
            onClick = {},
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
        Spacer(modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Sudah punya akun?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.padding(end = 4.dp)
            )
            Text(
                "Masuk",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppPurple,
                modifier = modifier.clickable {
                    navController.navigate(LoginRoute)
                }
            )

        }

    }

}
