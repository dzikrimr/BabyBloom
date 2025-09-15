package com.example.bubtrack.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bubtrack.R
import com.example.bubtrack.presentation.navigation.EditProfileRoute
import com.example.bubtrack.presentation.navigation.HelpAndReportRoute
import com.example.bubtrack.presentation.navigation.SettingRoute
import com.example.bubtrack.presentation.profile.comps.ProfileButton
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPurple

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    navigateLogin: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
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

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPurple)
            }
        } else {
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
                    if (userProfile.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.profileImageUrl,
                            contentDescription = "profile image",
                            contentScale = ContentScale.Crop,
                            modifier = modifier
                                .padding(top = 8.dp)
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.img_profileplaceholder),
                            contentDescription = "profile image",
                            contentScale = ContentScale.Crop,
                            modifier = modifier
                                .padding(top = 8.dp)
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    }

                    Text(
                        userProfile.name.ifEmpty { "User" },
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
                        modifier = modifier.fillMaxWidth()
                    ) {
                        ProfileButton(
                            text = "Edit Profil",
                            bgColor = AppPurple,
                            icon = R.drawable.ic_profil,
                            iconColor = AppPurple
                        ) { navController.navigate(EditProfileRoute) }

                        HorizontalDivider(thickness = 0.5.dp)

                        ProfileButton(
                            text = "Pengaturan",
                            bgColor = AppPurple,
                            icon = R.drawable.ic_settings,
                            iconColor = AppPurple
                        ) {
                            navController.navigate(SettingRoute)
                        }

                        HorizontalDivider(thickness = 0.5.dp)

                        ProfileButton(
                            text = "Bantuan & Laporan",
                            bgColor = AppBlue,
                            icon = R.drawable.ic_qna,
                            iconColor = AppBlue
                        ) { navController.navigate(HelpAndReportRoute) }
                    }
                }

                Spacer(modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        navigateLogin()
                    },
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
}