package com.example.phylaxfileaccess.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phylaxfileaccess.models.FileItem
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.FileActivityEvent
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.nativeCanvas

enum class ChartType { LINE, BAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(file: FileItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: FileViewModel = viewModel(factory = FileViewModelFactory(context))
    val events by viewModel.activityEvents.collectAsState()
    var selectedChartType by remember { mutableStateOf(ChartType.LINE) }

    LaunchedEffect(file.path) {
        viewModel.loadActivityEvents(file.path)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "FILE ACTIVITY",
                        color = PhylaxGreen,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PhylaxBlack
                )
            )
        },
        containerColor = PhylaxBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // File Info Header
            Surface(
                color = PhylaxCardBg.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, PhylaxGreen.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = PhylaxGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = file.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${events.size} activities recorded",
                            color = PhylaxGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHARE PACE COUNT",
                        color = PhylaxGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    
                    // Chart Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PhylaxCardBg)
                            .padding(2.dp)
                    ) {
                        ChartTypeButton(
                            selected = selectedChartType == ChartType.LINE,
                            icon = Icons.Default.ShowChart
                        ) { selectedChartType = ChartType.LINE }
                        
                        ChartTypeButton(
                            selected = selectedChartType == ChartType.BAR,
                            icon = Icons.Default.BarChart
                        ) { selectedChartType = ChartType.BAR }
                    }
                }

                ShareActivityChart(events, selectedChartType)
            }

            Text(
                text = "RECENT EVENTS",
                color = PhylaxGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
            )

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No activity recorded for this file.", color = PhylaxGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events.reversed()) { event ->
                        ActivityEventItem(
                            event = event
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartTypeButton(selected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) PhylaxGreen else Color.Transparent)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.Black else PhylaxGray,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ShareActivityChart(events: List<FileActivityEvent>, type: ChartType) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(events, type) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Surface(
        color = PhylaxCardBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChartLegendItem("Shared", PhylaxGreen)
                ChartLegendItem("Initiated", Color.Yellow)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data grouping logic
            val calendar = Calendar.getInstance()
            // Group by hour. If there's only one hour, we'll handle it.
            val hourGroups = events.groupBy { event ->
                calendar.timeInMillis = event.timestamp
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.toSortedMap()

            var chartData = hourGroups.map { (time, hourlyEvents) ->
                val sharedCount = hourlyEvents.count { it.eventType == "FILE_SHARED" }
                val initiatedCount = hourlyEvents.count { it.eventType == "OPEN_SHARE" }
                Triple(time, sharedCount, initiatedCount)
            }

            // If only one data point, add a dummy one at the start to make the line draw
            if (chartData.size == 1) {
                val first = chartData[0]
                chartData = listOf(
                    Triple(first.first - 3600000L, 0, 0),
                    first
                )
            }

            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, start = 30.dp)) {
                val width = size.width
                val height = size.height
                
                val maxCount = chartData.maxOfOrNull { it.second.coerceAtLeast(it.third) }?.coerceAtLeast(1) ?: 1
                
                // Draw Y-Axis
                val ySteps = 4
                for (i in 0..ySteps) {
                    val y = height - (height * (i.toFloat() / ySteps))
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        "${(maxCount * i) / ySteps}",
                        -25f,
                        y + 10f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 20f
                        }
                    )
                }

                if (chartData.isEmpty()) return@Canvas

                val spacing = width / (chartData.size - 1).toFloat()
                
                when (type) {
                    ChartType.LINE -> {
                        val sharedPath = Path()
                        val initiatedPath = Path()
                        
                        chartData.forEachIndexed { index, (_, shared, initiated) ->
                            val x = index * spacing
                            val yShared = height - (height * (shared.toFloat() / maxCount)) * animationProgress.value
                            val yInitiated = height - (height * (initiated.toFloat() / maxCount)) * animationProgress.value
                            
                            if (index == 0) {
                                sharedPath.moveTo(x, yShared)
                                initiatedPath.moveTo(x, yInitiated)
                            } else {
                                val prevX = (index - 1) * spacing
                                val prevYShared = height - (height * (chartData[index-1].second.toFloat() / maxCount)) * animationProgress.value
                                val prevYInitiated = height - (height * (chartData[index-1].third.toFloat() / maxCount)) * animationProgress.value

                                // Smooth curve
                                sharedPath.cubicTo(
                                    prevX + spacing / 2, prevYShared,
                                    x - spacing / 2, yShared,
                                    x, yShared
                                )
                                initiatedPath.cubicTo(
                                    prevX + spacing / 2, prevYInitiated,
                                    x - spacing / 2, yInitiated,
                                    x, yInitiated
                                )
                            }
                            
                            // Draw point circles
                            drawCircle(
                                color = PhylaxGreen,
                                radius = 4.dp.toPx(),
                                center = Offset(x, yShared)
                            )
                            drawCircle(
                                color = Color.Yellow,
                                radius = 3.dp.toPx(),
                                center = Offset(x, yInitiated)
                            )

                            drawXAxisLabel(x, chartData[index].first, index, chartData.size, height, animationProgress.value)
                        }

                        // Draw the lines
                        drawPath(sharedPath, PhylaxGreen, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(initiatedPath, Color.Yellow, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Draw area fill for shared
                        val fillPath = Path().apply {
                            addPath(sharedPath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(PhylaxGreen.copy(alpha = 0.2f * animationProgress.value), Color.Transparent)
                            )
                        )
                    }
                    ChartType.BAR -> {
                        val barWidth = (spacing * 0.4f).coerceAtMost(30.dp.toPx())
                        chartData.forEachIndexed { index, (_, shared, initiated) ->
                            val x = index * spacing
                            val hShared = (height * (shared.toFloat() / maxCount)) * animationProgress.value
                            val hInitiated = (height * (initiated.toFloat() / maxCount)) * animationProgress.value
                            
                            // Shared Bar
                            drawRect(
                                color = PhylaxGreen,
                                topLeft = Offset(x - barWidth, height - hShared),
                                size = Size(barWidth, hShared)
                            )
                            
                            // Initiated Bar
                            drawRect(
                                color = Color.Yellow.copy(alpha = 0.7f),
                                topLeft = Offset(x, height - hInitiated),
                                size = Size(barWidth, hInitiated)
                            )
                            
                            drawXAxisLabel(x, chartData[index].first, index, chartData.size, height, animationProgress.value)
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawXAxisLabel(
    x: Float,
    timestamp: Long,
    index: Int,
    total: Int,
    height: Float,
    progress: Float
) {
    if (index % ((total / 3).coerceAtLeast(1)) == 0 || index == total - 1) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        drawContext.canvas.nativeCanvas.drawText(
            timeStr,
            x - 25f,
            height + 35f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                alpha = (150 * progress).toInt()
                textSize = 22f
            }
        )
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = PhylaxGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActivityEventItem(event: FileActivityEvent) {
    val isShared = event.eventType == "FILE_SHARED"
    val color = if (isShared) PhylaxGreen else Color.Yellow
    val icon = if (isShared) Icons.Default.Share else Icons.Default.History
    val title = if (isShared) "File Shared" else "Share Initiated"
    val subtitle = if (isShared) "To: ${event.targetApp}" else "Share screen opened"

    Surface(
        color = PhylaxCardBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = PhylaxGray, fontSize = 11.sp)
                Text(
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(event.timestamp)),
                    color = PhylaxGray.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
