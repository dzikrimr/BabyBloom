package com.example.bubtrack.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bubtrack.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value : String,
    placeholder : String,
    onValueChange : (String) -> Unit
) {

    var showPassword by remember {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value =  value,
        onValueChange = {onValueChange(it)},
        modifier = modifier
            .fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE5E7EB),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            disabledContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        visualTransformation = if (showPassword){
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            if (showPassword){
                IconButton(
                    onClick = { showPassword = false}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_showpw),
                        contentDescription = ""
                    )
                }
            } else {
                IconButton(
                    onClick = { showPassword = true}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_hidepw),
                        contentDescription = ""
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp)

    )

}