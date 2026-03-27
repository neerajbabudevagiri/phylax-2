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
            .height(200.dp)
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
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Draw Grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (events.size < 2) {
                    // Draw a single point if only one event
                    val event = events.firstOrNull()
                    if (event != null) {
                        val y = if (event.eventType == "FILE_SHARED") height * 0.2f else height * 0.8f
                        drawCircle(
                            color = if (event.eventType == "FILE_SHARED") PhylaxGreen else Color.Yellow,
                            radius = 6.dp.toPx() * animationProgress.value,
                            center = Offset(width / 2, y)
                        )
                    }
                    return@Canvas
                }

                val spacing = width / (events.size - 1)
                val points = events.mapIndexed { index, event ->
                    val x = index * spacing
                    val y = if (event.eventType == "FILE_SHARED") height * 0.2f else height * 0.8f
                    Offset(x, y)
                }

                // Draw Area Gradient
                val fillPath = Path().apply {
                    moveTo(0f, height)
                    points.forEach { moveTo ->
                        lineTo(moveTo.x, height - (height - moveTo.y) * animationProgress.value)
                    }
                    lineTo(width, height)
                    close()
                }
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PhylaxGreen.copy(alpha = 0.15f * animationProgress.value), Color.Transparent)
                    )
                )

                // Draw Smooth Line
                val linePath = Path().apply {
                    points.forEachIndexed { index, point ->
                        val animatedY = height - (height - point.y) * animationProgress.value
                        if (index == 0) moveTo(point.x, animatedY)
                        else lineTo(point.x, animatedY)
                    }
                }

                drawPath(
                    path = linePath,
                    color = PhylaxGreen.copy(alpha = 0.6f * animationProgress.value),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Data Points
                points.forEachIndexed { index, point ->
                    val event = events[index]
                    val animatedY = height - (height - point.y) * animationProgress.value
                    
                    drawCircle(
                        color = if (event.eventType == "FILE_SHARED") PhylaxGreen else Color.Yellow,
                        radius = 5.dp.toPx() * animationProgress.value,
                        center = Offset(point.x, animatedY)
                    )
                    
                    drawCircle(
                        color = PhylaxBlack,
                        radius = 2.dp.toPx() * animationProgress.value,
                        center = Offset(point.x, animatedY)
                    )
                }
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
