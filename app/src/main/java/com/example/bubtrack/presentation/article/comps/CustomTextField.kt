package com.example.bubtrack.presentation.article.comps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.R
import com.example.bubtrack.ui.theme.AppGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Tips untuk parents",
    onClear: () -> Unit,
    onSearch: () -> Unit
) {
    Box(
        modifier = modifier.run {
            height(45.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.dp, AppGray, RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        },
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.labelMedium.copy(
                color = Color.Black,
                fontSize = 12.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSearch()
                }
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholderText,
                                color = AppGray,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (value.isNotEmpty()) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close), // Ganti ke ikon X
                            contentDescription = "Clear",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    onValueChange("")
                                    onClear()
                                }
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_search), // Ikon search biasa
                            contentDescription = "Search",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        )
    }
}