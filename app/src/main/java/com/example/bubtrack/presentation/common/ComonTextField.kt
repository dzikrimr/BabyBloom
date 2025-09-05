package com.example.bubtrack.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTextField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = singleLine,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFFE5E7EB),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
    )
}