package com.example.bubtrack.presentation.profile.comps

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndReportScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {

    var selectedIssue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val issueTypes = listOf("Bug", "Saran", "Lainnya")
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {navigateBack()}, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back button"
                )
            }
            Text(
                "Bantuan & Laporan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppPurple.copy(0.1f))
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppPurple.copy(0.2f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_qna),
                    contentDescription = null,
                    tint = AppPurple,
                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                )
            }
            Text(
                "Pusat Bantuan",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Text(
                "Temukan jawaban untuk pertanyaan yang sering diajukan",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FAQ List
        HelpCard(
            icon = R.drawable.ic_profil,
            title = "Bagaimana cara mengubah profil?",
            subtitle = "Buka menu Profil, lalu tap tombol Edit untuk mengubah informasi personal Anda.",
            backgroundColor = Color(0xFFEAF2FF),
            iconTint = Color(0xFF4A90E2)
        )
        HelpCard(
            icon = R.drawable.ic_lock,
            title = "Bagaimana cara reset password?",
            subtitle = "Gunakan fitur 'Lupa Password' di halaman login atau hubungi customer service.",
            backgroundColor = Color(0xFFFFEBEE),
            iconTint = Color(0xFFE57373)
        )
        HelpCard(
            icon = R.drawable.ic_notification,
            title = "Cara mengatur notifikasi?",
            subtitle = "Masuk ke Pengaturan > Notifikasi untuk menyesuaikan preferensi notifikasi Anda.",
            backgroundColor = Color(0xFFEAF2FF),
            iconTint = Color(0xFF4A90E2)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppPink.copy(0.1f))
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppPink.copy(0.2f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flag),
                    contentDescription = null,
                    tint = AppPink,
                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                )
            }
            Text(
                "Laporkan Masalah",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Text(
                "Bantu kami meningkatkan aplikasi dengan melaporkan bug atau masalah",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Form Laporan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dropdown Jenis Masalah
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedIssue,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Masalah") },
                        placeholder = { Text("Pilih jenis masalah") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5E7EB),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            disabledContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = AppBackground
                    ) {
                        issueTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedIssue = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Deskripsi Masalah
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi Masalah") },
                    placeholder = { Text("Jelaskan masalah yang Anda alami...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE5E7EB),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        disabledContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )

                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Kirim
                Button(
                    onClick = {
                        Toast.makeText(context, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kirim Laporan")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HelpCard(
    icon: Int,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
            ){
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp).align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
