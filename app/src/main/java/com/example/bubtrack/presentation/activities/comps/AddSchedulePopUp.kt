package com.example.bubtrack.presentation.activities.comps

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults

import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.bubtrack.R
import com.example.bubtrack.domain.activities.ActivityType
import com.example.bubtrack.presentation.common.CommonTextField
import com.example.bubtrack.presentation.onboarding.comps.DatePickerModal
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppBlue
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.ui.theme.BubTrackTheme
import com.example.bubtrack.utill.Utility
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSchedulePopUp(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, LocalTime?, String) -> Unit
) {
    var activityName by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var activityType by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("dd/mm/yyyy") }
    var dateMillis by remember { mutableLongStateOf(0) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val activityTypes = ActivityType.values().map { it.value }

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = modifier.padding(12.dp)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    IconButton(onClick = { onDismiss() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close"
                        )
                    }
                }
                Spacer(modifier.height(24.dp))
                Text(
                    "Activity Name",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = modifier.padding(bottom = 8.dp)
                )
                CommonTextField(
                    value = activityName,
                    placeholder = "Enter Activity Name",
                    onValueChange = { activityName = it}
                )
                Spacer(modifier.height(14.dp))
                Text(
                    "Activity Type",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = modifier.padding(bottom = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = activityType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activity Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5E7EB),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            disabledContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = AppBackground
                    ) {
                        activityTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    activityType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier.height(14.dp))
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = modifier.weight(1f)
                    ) {
                        Text(
                            "Date",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = modifier.padding(bottom = 8.dp)
                        )
                        DatePickerModal(
                            date = date,
                            onDateSelected = {
                                it?.let {
                                    date = Utility.formatDate(it)
                                    dateMillis = it
                                    errorMessage = null // Clear error on valid input
                                }
                            },
                            showIcon = false
                        )
                    }
                    Column (
                        modifier = modifier.weight(1f)
                    ){
                        Text(
                            "Time",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { showTimePicker = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedTime?.toString() ?: "--:--"
                            )
                        }
                    }
                }
                Spacer(modifier.height(14.dp))
                Text(
                    "Additional Notes",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE5E7EB),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        disabledContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                )
                Spacer(modifier.height(8.dp))
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (activityName.isBlank() || activityType.isBlank() || dateMillis == 0L || selectedTime == null) {
                            errorMessage = "Please fill all fields"
                        } else {
                            onSave(activityName, activityType, dateMillis, selectedTime, notes)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPurple),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(0.dp, Color.Transparent)
                ) {
                    Text(
                        "Simpan",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Time picker
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { time ->
                selectedTime = time
                showTimePicker = false
                errorMessage = null // Clear error on valid input
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState()

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        containerColor = Color.White,
                        clockDialColor = Color.White,
                        periodSelectorSelectedContainerColor = AppBlue,
                        timeSelectorSelectedContainerColor = AppPurple,
                        timeSelectorUnselectedContainerColor = Color(0xFFE5E7EB),
                        selectorColor = AppPurple
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        onConfirm(LocalTime.of(state.hour, state.minute))
                    }) {
                        Text("OK")
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
        AddSchedulePopUp(
            onDismiss = {},
            onSave = { _, _, _, _, _ -> }
        )
    }
}