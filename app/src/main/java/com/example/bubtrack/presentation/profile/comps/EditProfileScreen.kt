package com.example.bubtrack.presentation.profile.comps

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.onboarding.comps.DatePickerModal
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPink
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {


    var email by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0L) }
    var selectedGender by remember { mutableStateOf("Laki-Laki") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)

                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {navigateBack()},
                modifier = modifier.width(25.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = modifier.fillMaxSize()
                )
            }
            Text(
                "Edit Profil",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = modifier.width(25.dp))
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_profileplaceholder),
                contentDescription = "profile image",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
                    .size(80.dp)
                    .clip(CircleShape)
            )
            EditProfileField(
                value = "",
                onValueChange = {},
                label = "Nama"
            )
            EditProfileField(
                value = email,
                onValueChange = {email = it},
                label = "Email"
            )
            EditProfileField(
                value = "",
                onValueChange = {},
                label = "Nama Bayi"
            )
            Column {
                Text(
                    "Tanggal Lahir Bayi",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 5.dp)
                )
                DatePickerModal(date = date) {
                    it?.let { millis ->
                        dateMillis = millis
                        date = Utility.formatDate(millis)
                    }
                }
            }
            Column {
                Text(
                    "Gender",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 5.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { selectedGender = "Laki-Laki" },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedGender == "Laki-Laki") AppBlue else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedGender == "Laki-Laki") AppLightBlue else Color.White,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Laki-Laki", color = Color.Black)
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { selectedGender = "Perempuan" },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selectedGender == "Perempuan") AppPink else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedGender == "Perempuan") AppLightPink else Color.White,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Perempuan", color = Color.Black)
                    }
                }
            }

            OutlinedButton(
                onClick = {

                },
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppPurple,
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(width = 0.dp, color = Color.Transparent)
            ) {
                Text(
                    "Simpan",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

        }


    }
}


