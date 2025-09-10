package com.example.bubtrack.presentation.onboarding

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.common.NumberTextField
import com.example.bubtrack.presentation.navigation.MainRoute
import com.example.bubtrack.presentation.onboarding.comps.DatePickerModal
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPink
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility

@Composable
fun CreateProfileScreen(
    modifier: Modifier = Modifier,
    navController : NavController
) {

    var babyName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0) }
    var selectedGender by remember { mutableStateOf("Laki-Laki") }

    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var armCircumference by remember { mutableStateOf("") }

    // state page (0 = page 1, 1 = page 2)
    var currentPage by remember { mutableStateOf(0) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ayo Buat Profil si Kecil!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp, top = 24.dp)
            )
            Text(
                "Lacak pertumbuhan dan momen penting lebih mudah lewat profil personal.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        when (currentPage) {
            0 -> {
                Column {
                    Text(
                        "Nama Bayi",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    CommonTextField(
                        value = babyName,
                        placeholder = "Rizqeena",
                        onValueChange = { babyName = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tanggal Lahir Bayi",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    DatePickerModal(
                        date = date) {
                        it?.let { millis ->
                            dateMillis = millis
                            date = Utility.formatDate(millis)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
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
            }

            1 -> {
                Column {
                    Text(
                        "Berat Badan (kg)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    NumberTextField(
                        value = weight,
                        placeholder = "12",
                        onValueChange = { weight = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tinggi Badan (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    NumberTextField(
                        value = height,
                        placeholder = "12",
                        onValueChange = { height = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lingkar Kepala (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    NumberTextField(
                        value = headCircumference,
                        placeholder = "12",
                        onValueChange = { headCircumference = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lingkar Lengan (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 5.dp)
                    )
                    NumberTextField(
                        value = armCircumference,
                        placeholder = "12",
                        onValueChange = { armCircumference = it }
                    )
                }
            }
        }

        Column {
            OutlinedButton(
                onClick = {
                    if (currentPage == 0) {
                        if (babyName.isBlank() || dateMillis == 0L || selectedGender.isBlank()) {
                            Toast.makeText(context, "Lengkapi Nama, Tanggal Lahir, dan Gender!", Toast.LENGTH_SHORT).show()
                        } else {
                            currentPage = 1
                        }
                    } else {
                        if (weight.isBlank() || height.isBlank() || headCircumference.isBlank() || armCircumference.isBlank()) {
                            Toast.makeText(context, "Lengkapi semua data pada form ini!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Profil berhasil dibuat!", Toast.LENGTH_SHORT).show()
                            navController.navigate(MainRoute){
                                popUpTo(navController.graph.startDestinationId){
                                    inclusive = true
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPurple),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Text(
                    if (currentPage == 0) "Selanjutnya" else "Buat Profil",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (currentPage == 1) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { currentPage = 0 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AppPurple
                    ),
                    border = BorderStroke(1.dp, AppPurple),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Medium)
                }
            }
        }


    }
}


