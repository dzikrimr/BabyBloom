package com.example.bubtrack.presentation.profile.comps

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPink
import com.example.bubtrack.ui.theme.AppPurple
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navigateBack() }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back button"
                )
            }
            Text(
                "Pengaturan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifikasi Section
        SectionHeader(
            icon = R.drawable.ic_notification,
            title = "Notifikasi",
            subtitle = "Kelola pengaturan notifikasi",
            iconTint = AppPurple,
            bgTint = AppPurple.copy(0.2f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingCard {
            SettingSwitchItem(
                icon = R.drawable.ic_push,
                title = "Notifikasi Push",
                subtitle = "Terima notifikasi langsung",
            )

            SettingSwitchItem(
                icon = R.drawable.ic_speaker,
                title = "Suara Notifikasi",
                subtitle = "Aktifkan suara notifikasi"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privasi & Keamanan Section
        SectionHeader(
            icon = R.drawable.ic_privacy,
            title = "Privasi & Keamanan",
            subtitle = "Kelola izin aplikasi",
            iconTint = AppPink,
            bgTint = AppPink.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        SettingCard {
            // Camera Permission
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
            SettingSwitchItem(
                icon = R.drawable.ic_camera,
                title = "Akses Kamera",
                subtitle = "Izinkan akses kamera",
                initialChecked = cameraPermissionState.status.isGranted,
                onCheckedChange = {
                    if (cameraPermissionState.status.isGranted) {
                        // Redirect to system settings to revoke permission
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else if (cameraPermissionState.status.shouldShowRationale) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            )

            // Microphone Permission
            val micPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
            SettingSwitchItem(
                icon = R.drawable.ic_mic,
                title = "Akses Mikrofon",
                subtitle = "Izinkan akses mikrofon",
                initialChecked = micPermissionState.status.isGranted,
                onCheckedChange = {
                    if (micPermissionState.status.isGranted) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else if (micPermissionState.status.shouldShowRationale) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Request permission
                        micPermissionState.launchPermissionRequest()
                    }
                }
            )
        }
    }
}

@Composable
fun SectionHeader(icon: Int, title: String, subtitle: String, iconTint: Color, bgTint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color = bgTint)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(25.dp).align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingSwitchItem(
    icon: Int,
    title: String,
    subtitle: String,
    initialChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    var checked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                    .background(color = AppBlue.copy(alpha = 0.2f))
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = AppBlue,
                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange?.invoke(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppPurple,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}