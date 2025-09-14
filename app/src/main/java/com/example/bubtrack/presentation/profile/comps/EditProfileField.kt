package com.example.bubtrack.presentation.profile.comps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE5E7EB),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                disabledContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )
    }

}