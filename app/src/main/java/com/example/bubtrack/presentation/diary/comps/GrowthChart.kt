package com.example.bubtrack.presentation.diary.comps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bubtrack.presentation.diary.helper.GrowthUiState
import com.example.bubtrack.ui.theme.AppBackground
import com.example.bubtrack.ui.theme.AppPurple
import com.example.bubtrack.utill.Utility
import kotlin.math.roundToInt

@Composable
fun GrowthChart(
    state: GrowthUiState
) {
    var selectedChartType by remember { mutableStateOf<ChartType>(ChartType.Weight) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Growth Chart",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            // Dropdown Button
            Box {
                Card(
                    modifier = Modifier
                        .clickable { isDropdownExpanded = !isDropdownExpanded }
                        .width(120.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedChartType.displayName,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    containerColor = AppBackground
                ) {
                    listOf(
                        ChartType.Weight,
                        ChartType.Height,
                        ChartType.HeadCircumference,
                        ChartType.ArmLength
                    ).forEach { chartType ->
                        DropdownMenuItem(
                            text = { Text(chartType.displayName) },
                            onClick = {
                                selectedChartType = chartType
                                isDropdownExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        // Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.isError != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Terjadi kesalahan")
                    }
                }
                state.isSuccess.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada data pertumbuhan bayi")
                    }
                }
                else -> {
                    // Sort data by date (ascending)
                    val chartData = state.isSuccess.sortedBy { it.date }.map {
                        ChartData(
                            date = it.date,
                            weight = it.weight?.toFloat() ?: 0f,
                            height = it.height?.toFloat() ?: 0f,
                            headCircumference = it.headCircumference?.toFloat() ?: 0f,
                            armLength = it.armLength?.toFloat() ?: 0f
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        if (chartData.size < 3) {
                            Text(
                                "Data masih terbatas (${chartData.size} entri)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            text = "${selectedChartType.displayName} (${selectedChartType.unit})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LineChart(
                            data = chartData,
                            chartType = selectedChartType,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<ChartData>,
    chartType: ChartType,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height - 40.dp.toPx()

        // Get values based on chart type
        val values = data.map { chartData ->
            when (chartType) {
                is ChartType.Weight -> chartData.weight
                is ChartType.Height -> chartData.height
                is ChartType.HeadCircumference -> chartData.headCircumference
                is ChartType.ArmLength -> chartData.armLength
            }
        }

        val maxValue = values.maxOrNull() ?: 0f
        val minValue = values.minOrNull() ?: 0f
        val valueRange = if (maxValue == minValue) 1f else maxValue - minValue

        // Draw grid lines and Y-axis labels
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = canvasHeight - (i * canvasHeight / gridLines)
            val value = minValue + (i * valueRange / gridLines)
            // Draw grid line
            drawLine(
                color = Color(0xFFE5E7EB),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1.dp.toPx()
            )
            // Draw Y-axis label
            drawContext.canvas.nativeCanvas.drawText(
                "${value.roundToInt()}",
                -40f,
                y + 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                }
            )
        }

        // Calculate points
        val points = values.mapIndexed { index, value ->
            val x = if (values.size > 1) {
                index * canvasWidth / (values.size - 1)
            } else {
                canvasWidth / 2
            }
            val y = canvasHeight - ((value - minValue) / valueRange * canvasHeight)
            Offset(x, y)
        }

        // Draw line
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
        }
        drawPath(
            path = path,
            color = AppPurple,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = AppPurple,
                radius = 4.dp.toPx(),
                center = point
            )
        }

        // Draw X-axis labels
        data.forEachIndexed { index, chartData ->
            val x = if (data.size > 1) {
                index * canvasWidth / (data.size - 1)
            } else {
                canvasWidth / 2
            }
            val y = canvasHeight + 30.dp.toPx()
            val formattedDate = Utility.formatDate(chartData.date)
            drawContext.canvas.nativeCanvas.drawText(
                formattedDate,
                x - 40f,
                y,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                }
            )
        }
    }
}

data class ChartData(
    val date: Long,
    val weight: Float,
    val height: Float,
    val headCircumference: Float,
    val armLength: Float
)

sealed class ChartType(val displayName: String, val unit: String) {
    object Weight : ChartType("Berat", "kg")
    object Height : ChartType("Tinggi", "cm")
    object HeadCircumference : ChartType("L. Kepala", "cm")
    object ArmLength : ChartType("L. Lengan", "cm")
}