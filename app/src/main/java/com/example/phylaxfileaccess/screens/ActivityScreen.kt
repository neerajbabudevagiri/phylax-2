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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.FileActivityEvent
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.nativeCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(file: FileItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: FileViewModel = viewModel(factory = FileViewModelFactory(context))
    val events by viewModel.activityEvents.collectAsState()

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
                Text(
                    text = "SHARE PACE COUNT",
                    color = PhylaxGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                )

                ShareActivityChart(events)
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
fun ShareActivityChart(events: List<FileActivityEvent>) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(events) {
        animationProgress.animateTo(1f, animationSpec = tween(1500))
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
            
            // Group events by hour to show "Pace" accurately
            val calendar = Calendar.getInstance()
            val hourGroups = events.groupBy { event ->
                calendar.timeInMillis = event.timestamp
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }.toSortedMap()

            val chartData = hourGroups.map { (time, hourlyEvents) ->
                val sharedCount = hourlyEvents.count { it.eventType == "FILE_SHARED" }
                val initiatedCount = hourlyEvents.count { it.eventType == "OPEN_SHARE" }
                Triple(time, sharedCount, initiatedCount)
            }

            Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, start = 30.dp)) {
                val width = size.width
                val height = size.height
                
                val maxCount = chartData.maxOfOrNull { it.second.coerceAtLeast(it.third) }?.coerceAtLeast(1) ?: 1
                
                // Draw Y-Axis Grid & Labels
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

                val spacing = width / (chartData.size.coerceAtLeast(2) - 1).coerceAtLeast(1)
                
                // Paths for Shared (Green) and Initiated (Yellow)
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
                        sharedPath.lineTo(x, yShared)
                        initiatedPath.lineTo(x, yInitiated)
                    }

                    // Draw time labels on X-Axis
                    if (index % ((chartData.size / 4).coerceAtLeast(1)) == 0 || index == chartData.size - 1) {
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chartData[index].first))
                        drawContext.canvas.nativeCanvas.drawText(
                            timeStr,
                            x - 25f,
                            height + 35f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                alpha = (150 * animationProgress.value).toInt()
                                textSize = 22f
                            }
                        )
                    }
                }

                // Draw Shared Line (Green)
                drawPath(
                    path = sharedPath,
                    color = PhylaxGreen.copy(alpha = 0.8f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Initiated Line (Yellow)
                drawPath(
                    path = initiatedPath,
                    color = Color.Yellow.copy(alpha = 0.8f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Area Fill for Shared
                val fillPath = Path().apply {
                    addPath(sharedPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PhylaxGreen.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
            }
        }
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
