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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bubtrack.R
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.navigation.MainRoute
import com.example.bubtrack.presentation.navigation.RegisterRoute
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(14.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Selamat Datang, Parent",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            "Masuk untuk terus mendampingi si kecil.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier.height(32.dp))
        CommonTextField(
            value = email,
            placeholder = "Email",
            onValueChange = { email = it }
        )
        Spacer(modifier.height(12.dp))
        CommonTextField(
            value = password,
            placeholder = "Password",
            onValueChange = { password = it }
        )
        TextButton(
            onClick = {},
            modifier = modifier.align(Alignment.End)
        ) {
            Text(
                "Lupa Sandi?",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = AppPurple,
            )
        }
        Spacer(modifier.height(32.dp))
        OutlinedButton(
            onClick = {
                navController.navigate(MainRoute){
                    popUpTo(navController.graph.startDestinationId){
                        inclusive = true
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
                "Masuk",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
        Text(
            "Atau",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(vertical = 16.dp)
        )
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
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
                    modifier = modifier.size(25.dp)
                )
                Spacer(modifier.width(8.dp))
                Text(
                    "Masuk dengan Google",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppPurple
                )
            }

        }
        Spacer(modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Belum punya akun?",
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.padding(end = 4.dp)
            )
            Text(
                "Daftar",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppPurple,
                modifier = modifier.clickable{
                    navController.navigate(RegisterRoute)
                }
            )

        }

    }
}
