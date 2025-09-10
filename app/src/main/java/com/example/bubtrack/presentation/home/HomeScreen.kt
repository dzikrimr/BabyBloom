package com.example.bubtrack.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bubtrack.R
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.presentation.common.ScheduleCard
import com.example.bubtrack.presentation.diary.comps.StatsCard
import com.example.bubtrack.presentation.diary.comps.StatsCardItem
import com.example.bubtrack.presentation.navigation.ActivitiesRoute
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPurple
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import android.widget.Toast

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var babyProfile by remember { mutableStateOf<Map<String, Any>?>(null) }
    var babyAge by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Fetch baby profile data using LaunchedEffect
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("babyProfiles").document("primary")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        babyProfile = document.data
                        val birthDateMillis = document.getLong("birthDate") ?: 0L
                        if (birthDateMillis != 0L) {
                            val birthDate = Instant.ofEpochMilli(birthDateMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val today = LocalDate.now()
                            val months = ChronoUnit.MONTHS.between(birthDate, today).toInt()
                            val weeks = ChronoUnit.WEEKS.between(birthDate.plusMonths(months.toLong()), today).toInt()
                            babyAge = "$months Bulan, $weeks Minggu"
                        }
                    } else {
                        Toast.makeText(context, "Profil bayi tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Gagal mengambil data profil: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    val stats = babyProfile?.let {
        GrowthStats(
            weight = it["weight"] as? Double ?: 0.0,
            height = it["height"] as? Double ?: 0.0,
            headCircum = it["headCircumference"] as? Double ?: 0.0,
            armCircum = it["armCircumference"] as? Double ?: 0.0
        )
    } ?: GrowthStats(weight = 0.0, height = 0.0, headCircum = 0.0, armCircum = 0.0)

    val statsList = listOf(
        StatsCardItem(
            title = "Berat",
            value = stats.weight,
            icon = R.drawable.ic_weightscale,
            unit = "kg",
            bgColor = AppPurple
        ),
        StatsCardItem(
            title = "Tinggi",
            value = stats.height,
            icon = R.drawable.ic_height,
            unit = "cm",
            bgColor = AppBlue
        ),
        StatsCardItem(
            title = "L. Kepala",
            value = stats.headCircum,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppPink
        ),
        StatsCardItem(
            title = "L. Lengan",
            value = stats.armCircum,
            icon = R.drawable.ic_head,
            unit = "cm",
            bgColor = AppLightPurple
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 30.dp,
                            bottomEnd = 30.dp
                        )
                    )
                    .background(color = AppPurple)
                    .padding(top = 24.dp)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "BabyGrow",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Text(
                            "Halo, ${babyProfile?.get("babyName") ?: "Sarah"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 18.dp
                    )
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
                    .background(color = AppBlue)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Bayimu Sekarang",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        babyAge,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Box(
                        modifier = modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF93C5FD))
                            .padding(
                                vertical = 6.dp,
                                horizontal = 8.dp
                            )
                    ) {
                        Text(
                            "Growing Well",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
                Box(
                    modifier = modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                )
            }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    horizontal = 14.dp
                )
        ) {
            Text(
                "Smart Baby Care",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .clip(
                            RoundedCornerShape(24.dp)
                        )
                        .background(color = AppLightPurple)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                color = Color(0xFFA78BFA)
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Cry Analyzer",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Understand baby's \n need",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = modifier
                        .fillMaxHeight()
                        .fillMaxWidth(1f)
                        .clip(
                            RoundedCornerShape(24.dp)
                        )
                        .background(color = AppLightBlue)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                color = Color(0xFF93C5FD)
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Sleep Monitor",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Monitor baby's sleep",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Pertumbuhan Anak",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Lihat Semua",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppPurple)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statsList.forEach {
                    StatsCard(
                        statsCardItem = it,
                        width = 90,
                        height = 90
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Jadwal Terdekat",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Lihat Semua",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppPurple),
                    modifier = modifier.clickable {
                        navController.navigate(ActivitiesRoute)
                    }
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            ScheduleCard(
                title = "Vaksin Polio",
                location = "Puskesmas Glonggong",
                date = "12 Mei 2023"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BubTrackTheme {
        HomeScreen(navController = rememberNavController())
    }
}