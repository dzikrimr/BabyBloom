package com.example.bubtrack.presentation.ai.comps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme

@Composable
fun BabyCareCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    img: Int,
    btnText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = img),
                contentDescription = title,
                modifier = modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    ),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier.height(18.dp))
                OutlinedButton(
                    onClick = {onClick()},
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppPurple,
                    ),
                    border = BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent
                    ),
                    modifier = modifier.fillMaxWidth().height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = ""
                        )
                        Spacer(modifier.width(8.dp))
                        Text(
                            btnText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

        }
    }
}

@Preview
@Composable
private fun Preview() {
    BubTrackTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            BabyCareCard(
                title = "Cry Detection & Analysis",
                description = "Advanced AI analyzes your baby's cries to identify their needs instantly",
                img = R.drawable.img_cry,
                btnText = "Start Cry Analysis"
            ) { }
        }
    }
}