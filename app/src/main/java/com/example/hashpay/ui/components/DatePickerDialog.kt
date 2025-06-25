package com.example.hashpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.hashpay.ui.theme.SpaceGrotesk
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1F1F1F)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Due Date",
                    color = Color.White,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFF1F1F1F),
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.White,
                        subheadContentColor = Color.White,
                        yearContentColor = Color.White,
                        currentYearContentColor = Color(0xFF9FE870),
                        selectedYearContentColor = Color.Black,
                        selectedYearContainerColor = Color(0xFF9FE870),
                        dayContentColor = Color.White,
                        selectedDayContentColor = Color.Black,
                        selectedDayContainerColor = Color(0xFF9FE870),
                        todayContentColor = Color(0xFF9FE870),
                        todayDateBorderColor = Color(0xFF9FE870),
                        dayInSelectionRangeContentColor = Color.White,
                        dayInSelectionRangeContainerColor = Color(0xFF3A3A1F)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = SpaceGrotesk
                        )
                    }

                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateSelected(it)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF9FE870)
                        )
                    ) {
                        Text(
                            "OK",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}