package com.example.bubtrack.presentation.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.presentation.profile.comps.ProfileButton
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
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
                onClick = {},
                modifier = modifier.width(25.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "back button",
                    modifier = modifier.fillMaxSize()
                )
            }
            Text(
                "Profile",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = modifier.width(25.dp))
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Column(
                modifier = modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = modifier
                        .padding(bottom = 8.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppPurple)
                )
                Text(
                    "Budi Speed",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier.height(18.dp))
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()

                ) {
                    ProfileButton(
                        text = "Informasi Pribadi",
                        bgColor = AppPurple,
                        icon = R.drawable.ic_profil,
                        iconColor = AppPurple
                    ) { }
                    HorizontalDivider(
                        thickness = 0.5.dp
                    )
                    ProfileButton(
                        text = "Notifikasi",
                        bgColor = AppBlue,
                        icon = R.drawable.ic_notification,
                        iconColor = AppBlue
                    ) { }
                    HorizontalDivider(
                        thickness = 0.5.dp
                    )
                    ProfileButton(
                        text = "Pengaturan",
                        bgColor = AppPurple,
                        icon = R.drawable.ic_settings,
                        iconColor = AppPurple
                    ) { }
                    HorizontalDivider(
                        thickness = 0.5.dp
                    )
                    ProfileButton(
                        text = "Bantuan & Laporan",
                        bgColor = AppBlue,
                        icon = R.drawable.ic_qna,
                        iconColor = AppBlue
                    ) { }
                    HorizontalDivider(
                        thickness = 0.5.dp
                    )
                    ProfileButton(
                        text = "Berlangganan",
                        bgColor = AppPurple,
                        icon = R.drawable.ic_subs,
                        iconColor = AppPurple
                    ) { }
                }
            }

            Spacer(modifier.weight(1f))
            OutlinedButton(
                onClick = {},
                modifier = modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                ),
                border = BorderStroke(2.dp, Color(0xFFF87171))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center

                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout),
                        contentDescription = "logout",
                        tint = Color.Unspecified
                    )
                    Spacer(modifier.width(8.dp))
                    Text(
                        "Logout",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF87171)
                        )
                    )
                }
            }
            Spacer(modifier.height(18.dp))
        }

    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        ProfileScreen()
    }

}