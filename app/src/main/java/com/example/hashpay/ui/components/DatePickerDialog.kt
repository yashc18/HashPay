package com.example.hashpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Due Date",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.White,
                        dayContentColor = Color.White,
                        selectedDayContainerColor = Color(0xFF7CFF50),
                        todayContentColor = Color(0xFF7CFF50),
                        todayDateBorderColor = Color(0xFF7CFF50)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateSelected(it)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7CFF50),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}