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
import androidx.compose.foundation.layout.width
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun GrowthChartScreen(
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val chartState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0L) }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var headCircumference by remember { mutableStateOf("") }
    var armCircumference by remember { mutableStateOf("") }
    var ageInMonths by remember { mutableStateOf("") }
    val context = LocalContext.current
    val babyStats by viewModel.babyStats.collectAsState()

    // Dynamically get the latest stats from Firestore
    val latestStats = chartState.isSuccess

    val statsList = listOf(
        StatsCardItem(
            title = "Berat",
            value = babyStats?.weight ?: 0.0,
            icon = R.drawable.ic_weightscale,
            unit = "kg",
            bgColor = AppPurple
        ),
        StatsCardItem(
            title = "Tinggi",
            value = babyStats?.height ?: 0.0,
            icon = R.drawable.ic_height,
            unit = "cm",
            bgColor = AppBlue
        ),
        StatsCardItem(
            title = "L. Kepala",
            value = babyStats?.headCircum ?: 0.0,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppPink
        ),
        StatsCardItem(
            title = "L. Lengan",
            value = babyStats?.armCircum ?: 0.0,
            icon = R.drawable.ic_lengan,
            unit = "cm",
            bgColor = AppLightPurple
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Perkembangan Bayi",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            statsList.forEach {
                StatsCard(
                    statsCardItem = it,
                    height = 75,
                    width = 75
                )
            }
        }
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
                    placeholder = babyStats?.weight?.roundToInt().toString(),
                    onValueChange = { weight = it }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Tinggi Badan (cm)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                NumberTextField(
                    value = height,
                    placeholder = babyStats?.height?.roundToInt().toString(),
                    onValueChange = { height = it }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Lingkar Kepala (cm)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                NumberTextField(
                    value = headCircumference,
                    placeholder = babyStats?.headCircum?.roundToInt().toString(),
                    onValueChange = { headCircumference = it }
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Lingkar Lengan (cm)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                NumberTextField(
                    value = armCircumference,
                    placeholder = babyStats?.armCircum?.roundToInt().toString(),
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
                            val ageInMonthsValue = ageInMonths.toIntOrNull()
                            if (weightValue == null || heightValue == null ||
                                headCircumValue == null || armCircumValue == null ||
                                ageInMonthsValue == null
                            ) {
                                Toast.makeText(context, "Input harus berupa angka valid!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addGrowthRecord(
                                    date = dateMillis,
                                    weight = weightValue,
                                    height = heightValue,
                                    headCircumference = headCircumValue,
                                    armLength = armCircumValue,
                                    ageInMonths = ageInMonthsValue
                                )
                                // Reset form
                                isExpanded = false
                                date = "dd/mm/yyyy"
                                dateMillis = 0L
                                weight = ""
                                height = ""
                                headCircumference = ""
                                armCircumference = ""
                                ageInMonths = ""
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
        GrowthChart(chartState)
        Text(
            "Pengukuran Terbaru",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        if (chartState.isSuccess.isNotEmpty()) {
            chartState.isSuccess.forEach { growth ->
                GrowthCard(babyGrowth = growth)
            }
        } else {
            Text(
                text = if (selectedDate == null) "Belum ada data perkembangan" else "Tidak ada data perkembangan pada tanggal ini",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
    }
}
