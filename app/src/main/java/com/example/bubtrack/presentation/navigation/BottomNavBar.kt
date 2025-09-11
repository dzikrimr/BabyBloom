package com.example.bubtrack.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {

    val navItems = listOf(
        NavItem(
            title = "Beranda",
            icon = R.drawable.ic_home
        ),
        NavItem(
            title = "Diary",
            icon = R.drawable.ic_diary
        ),
        NavItem(
            title = "AI",
            icon = R.drawable.ic_ai
        ),
        NavItem(
            title = "Artikel",
            icon = R.drawable.ic_artikel
        ),
        NavItem(
            title = "Profil",
            icon = R.drawable.ic_profil
        ),
    )

    NavigationBar(
        containerColor = Color.White,
        modifier = modifier
    ) {
        navItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = { onItemClick(index)},
                icon = {
                    if (selectedItem == index) {
                        Box(
                            modifier = Modifier
                                .size(32.dp) // ukuran lingkaran
                                .clip(CircleShape)
                                .background(AppPurple), // pakai warna Anda
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = Color.Gray
                        )
                    }
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (selectedItem == index) AppPurple else Color.Gray
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}


data class NavItem(
    val title: String,
    val icon: Int
)
