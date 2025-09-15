package com.example.bubtrack.presentation.profile.comps

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.bubtrack.R
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.onboarding.comps.DatePickerModal
import com.example.bubtrack.presentation.profile.ProfileViewModel
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppLightBlue
import com.example.bubtrack.ui.theme.AppLightPink
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.utill.Resource
import com.example.bubtrack.utill.Utility

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var babyName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0L) }
    var selectedGender by remember { mutableStateOf("Laki-Laki") }
    var profileImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadProfileImage(it)
        }
    }

    // Load user data when screen loads
    LaunchedEffect(userProfile) {
        if (userProfile.name.isNotEmpty()) {
            name = userProfile.name
            email = userProfile.email
            babyName = userProfile.babyName
            selectedGender = userProfile.gender.ifEmpty { "Laki-Laki" }
            profileImageUrl = userProfile.profileImageUrl
            if (userProfile.birthDate > 0) {
                dateMillis = userProfile.birthDate
                date = Utility.formatDate(userProfile.birthDate)
            }
        }
    }

    // Handle upload state
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is Resource.Success -> {
                uploadState.data?.let { url ->
                    profileImageUrl = url
                    Toast.makeText(context, "Foto profil berhasil diunggah", Toast.LENGTH_SHORT).show()
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, "Gagal mengunggah foto: ${uploadState.msg}", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

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
                onClick = { navigateBack() },
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
            // Profile Image with Edit Button
            Box(
                modifier = modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp)
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "profile image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "profile image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.img_profileplaceholder),
                        contentDescription = "profile image",
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }

                // Edit button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uploadState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit profile picture",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            EditProfileField(
                value = name,
                onValueChange = { name = it },
                label = "Nama"
            )

            EditProfileField(
                value = email,
                onValueChange = { },
                label = "Email",
                enabled = false
            )

            EditProfileField(
                value = babyName,
                onValueChange = { babyName = it },
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
                    if (name.isNotEmpty() && babyName.isNotEmpty() && dateMillis > 0) {
                        viewModel.updateProfile(
                            name = name,
                            babyName = babyName,
                            birthDate = dateMillis,
                            gender = selectedGender,
                            profileImageUrl = profileImageUrl.ifEmpty { null }
                        )
                        Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        navigateBack()
                    } else {
                        Toast.makeText(context, "Harap isi semua field", Toast.LENGTH_SHORT).show()
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
                border = BorderStroke(width = 0.dp, color = Color.Transparent),
                enabled = !isLoading && uploadState !is Resource.Loading
            ) {
                if (isLoading || uploadState is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "Simpan",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
        }
    }
}