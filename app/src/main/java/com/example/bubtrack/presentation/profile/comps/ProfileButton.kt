package com.example.bubtrack.presentation.profile.comps


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppPurple

@Composable
fun ProfileButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: Int,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp)
            .clickable{
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color = bgColor.copy(alpha = 0.2f))
            ){
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "info",
                    tint = iconColor,
                    modifier = modifier.size(15.dp).align(Alignment.Center)
                )
            }
            Spacer(modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Icon(
            Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = ""
        )
    }
}