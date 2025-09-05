package com.example.bubtrack.presentation.diary

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.domain.diary.DiaryModel
import com.example.bubtrack.ui.theme.AppGray
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun DevelopmentScreen(modifier: Modifier = Modifier) {

    val diary = listOf<DiaryModel?>(null)
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        OutlinedButton(
            onClick = {

            },
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
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppPurple)
                    ){
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "add",
                            modifier = modifier.align(Alignment.Center).size(20.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier.width(12.dp))
                    Text(
                        "Tambah Pencapaian",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.Black
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrowdown),
                    "arrow",
                    tint = AppGray
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDev() {
    BubTrackTheme {
        DevelopmentScreen()
    }
}