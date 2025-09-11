package com.example.bubtrack.presentation.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bubtrack.R
import com.example.bubtrack.domain.growth.BabyGrowth
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.presentation.common.NumberTextField
import com.example.bubtrack.presentation.diary.comps.GrowthCard
import com.example.bubtrack.presentation.diary.comps.GrowthChart
import com.example.bubtrack.presentation.diary.comps.StatsCard
import com.example.bubtrack.presentation.diary.comps.StatsCardItem
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import com.example.bubtrack.presentation.onboarding.comps.DatePickerModal
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppGray
import com.example.bubtrack.ui.theme.AppLightPurple
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun GrowthChartScreen(
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val chartState by viewModel.uiState.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0L) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var armCircumference by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Dynamically get the latest stats from Firestore
    val latestStats = chartState.isSuccess.firstOrNull()?.let {
        GrowthStats(
            weight = it.weight ?: 0.0,
            height = it.height ?: 0.0,
            headCircum = it.headCircumference ?: 0.0,
            armCircum = it.armLength ?: 0.0
        )
    } ?: GrowthStats(weight = 0.0, height = 0.0, headCircum = 0.0, armCircum = 0.0)

    val statsList = listOf(
        StatsCardItem(
            title = "Berat",
            value = latestStats.weight,
            icon = R.drawable.ic_weightscale,
            unit = "kg",
            bgColor = AppPurple
        ),
        StatsCardItem(
            title = "Tinggi",
            value = latestStats.height,
            icon = R.drawable.ic_height,
            unit = "cm",
            bgColor = AppBlue
        ),
        StatsCardItem(
            title = "L. Kepala",
            value = latestStats.headCircum,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppPink
        ),
        StatsCardItem(
            title = "L. Lengan",
            value = latestStats.armCircum,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppLightPurple
        ),
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Current Stats",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statsList.forEach {
                    StatsCard(statsCardItem = it)
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3F4F6),
                ),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(width = 0.dp, color = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppPurple)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = "add",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(20.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Catat Perkembangan",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.Black
                        )
                    }
                    Icon(
                        painter = painterResource(if (isExpanded) R.drawable.ic_arrowup else R.drawable.ic_arrowdown),
                        contentDescription = "arrow",
                        tint = AppGray
                    )
                }
            }
        }
        item {
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                visible = isExpanded
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Tanggal",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    DatePickerModal(date = date) {
                        it?.let { millis ->
                            dateMillis = millis
                            date = Utility.formatDate(millis)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Berat Badan (kg)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    NumberTextField(
                        value = weight,
                        placeholder = "12",
                        onValueChange = { weight = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tinggi Badan (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    NumberTextField(
                        value = height,
                        placeholder = "12",
                        onValueChange = { height = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lingkar Kepala (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    NumberTextField(
                        value = headCircumference,
                        placeholder = "12",
                        onValueChange = { headCircumference = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lingkar Lengan (cm)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    NumberTextField(
                        value = armCircumference,
                        placeholder = "12",
                        onValueChange = { armCircumference = it }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            if (dateMillis == 0L || weight.isBlank() || height.isBlank() ||
                                headCircumference.isBlank() || armCircumference.isBlank()
                            ) {
                                Toast.makeText(context, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                            } else {
                                val weightValue = weight.toDoubleOrNull()
                                val heightValue = height.toDoubleOrNull()
                                val headCircumValue = headCircumference.toDoubleOrNull()
                                val armCircumValue = armCircumference.toDoubleOrNull()

                                if (weightValue == null || heightValue == null ||
                                    headCircumValue == null || armCircumValue == null
                                ) {
                                    Toast.makeText(context, "Input harus berupa angka valid!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addGrowthRecord(
                                        date = dateMillis,
                                        weight = weightValue,
                                        height = heightValue,
                                        headCircumference = headCircumValue,
                                        armLength = armCircumValue,
                                        ageInMonths = 0 // Adjust if age calculation is needed
                                    )
                                    // Reset form
                                    isExpanded = false
                                    date = "dd/mm/yyyy"
                                    dateMillis = 0L
                                    weight = ""
                                    height = ""
                                    headCircumference = ""
                                    armCircumference = ""
                                    Toast.makeText(context, "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
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
        item {
            GrowthChart(chartState)
        }
        item {
            Text(
                "Pengukuran Terbaru",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        if (chartState.isSuccess.isNotEmpty()) {
            items(chartState.isSuccess) { growth ->
                GrowthCard(babyGrowth = growth)
            }
        } else {
            item {
                Text(
                    "Belum ada data perkembangan",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrowthChartScreenPreview() {
    // Mock DiaryViewModel interface without inheritance
    val mockUiState = MutableStateFlow(
        GrowthUiState(
            isSuccess = listOf(
                BabyGrowth(
                    id = "1",
                    date = System.currentTimeMillis(),
                    weight = 3.2,
                    height = 52.0,
                    headCircumference = 35.0,
                    armLength = 18.0,
                    ageInMonths = 0
                ),
                BabyGrowth(
                    id = "2",
                    date = System.currentTimeMillis() - 86400000, // 1 day ago
                    weight = 3.0,
                    height = 50.0,
                    headCircumference = 34.5,
                    armLength = 17.5,
                    ageInMonths = 0
                )
            )
        )
    )

    // Create a mock DiaryViewModel without extending the original class
    val mockViewModel = object {
        val uiState: StateFlow<GrowthUiState> = mockUiState
        fun addGrowthRecord(
            date: Long,
            weight: Double,
            height: Double,
            headCircumference: Double,
            armLength: Double,
            ageInMonths: Int
        ) {
            // No-op for preview
        }
    }

    BubTrackTheme {
        GrowthChartScreen(
            modifier = Modifier.padding(16.dp),
            viewModel = mockViewModel as DiaryViewModel
        )
    }
}