package com.example.bubtrack.presentation.diary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.bubtrack.R
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.presentation.diary.comps.DiaryCard
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppGray
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale

@Composable
fun DevelopmentScreen(
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var diaryTitle by rememberSaveable { mutableStateOf("") }
    var diaryDescription by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it.toString()
        }
    }
    val diaryList by viewModel.diaryState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Tambah Pencapaian",
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Judul Pencapaian",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = diaryTitle,
                    onValueChange = { diaryTitle = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = AppGray,
                        errorBorderColor = AppGray,
                        focusedBorderColor = AppGray,
                        disabledBorderColor = AppGray
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Deskripsi",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = diaryDescription,
                    onValueChange = { diaryDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = AppGray,
                        errorBorderColor = AppGray,
                        focusedBorderColor = AppGray,
                        disabledBorderColor = AppGray
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Tambah Foto",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(
                                width = 1.dp,
                                color = AppGray,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3F4F6))
                            .clickable { pickImageLauncher.launch("image/*") }
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = "camera button",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(30.dp)
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        if (diaryTitle.isBlank() || diaryDescription.isBlank()) {
                            Toast.makeText(context, "Judul dan deskripsi harus diisi!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addDiaryEntry(
                                title = diaryTitle,
                                description = diaryDescription,
                                imageUri = selectedImageUri,
                                context = context
                            )
                            diaryTitle = ""
                            diaryDescription = ""
                            selectedImageUri = null
                            isExpanded = false
                        }
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
        if (diaryList.isNotEmpty()) {
            diaryList.forEach { diary ->
                DiaryCard(diary = diary)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (selectedDate == null) "Belum ada diary :(" else "Tidak ada diary pada tanggal ini",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
    }
}

@Preview
@Composable
private fun PreviewDev() {
    BubTrackTheme {
        DevelopmentScreen()
    }
}