package com.example.bubtrack.presentation.diary.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bubtrack.domain.diary.Diary
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility

@Composable
fun DiaryCard(
    modifier: Modifier = Modifier,
    diary: Diary
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    diary.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal
                    )
                )
                Text(
                    text = Utility.formatPrettyDate(diary.date),
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = modifier.padding(top = 8.dp)
                )
            }
            if (diary.imgUrl != null) {
                AsyncImage(
                    model = diary.imgUrl,
                    contentDescription = "diary image",
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppBlue)
                )
            }
        }
        Text(
            diary.desc,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(top = 14.dp, end = 55.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppPurple)
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DiaryCard(
                diary = Diary(
                    title = "Test Title",
                    desc = "Test Description aowkaowkaowkoawkwa keren mantep anjay banget askdjasl askldjas asdlkajsd asdlkajs asdlkjasas adskjsa kdajdkas asdkjask adskj",
                    id = "1",
                    date = System.currentTimeMillis(),
                    imgUrl = null
                )
            )
        }
    }
}