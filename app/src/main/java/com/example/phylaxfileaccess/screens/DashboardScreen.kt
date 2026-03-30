package com.example.phylaxfileaccess.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.CategoryInfo
import com.example.phylaxfileaccess.models.StorageInfo
import com.example.phylaxfileaccess.ui.theme.*
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import com.example.phylaxfileaccess.utils.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FileViewModel = viewModel(
        factory = FileViewModelFactory(context)
    )

    val storage by viewModel.storage.collectAsState()
    val categoriesInfo by viewModel.categoriesInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStorage()
        viewModel.loadAllCategoriesInfo()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DASHBOARD",
                        color = PhylaxGreen,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StorageDonutChart(storage)

            Spacer(Modifier.height(32.dp))

            SectionTitle("Storage Breakdown")
            
            Spacer(Modifier.height(16.dp))

            categoriesInfo.forEach { info ->
                CategoryStorageItem(info, storage?.totalSpace ?: 1L)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))

            SectionTitle("Available Volumes")
            
            Spacer(Modifier.height(16.dp))

            StorageMetricsPanel(storage)

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun StorageDonutChart(storage: StorageInfo?) {
    val used = storage?.usedSpace ?: 0L
    val total = storage?.totalSpace ?: 1L
    val percent = (used.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "storagePercent"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Track
            drawArc(
                color = Color.White.copy(0.05f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(PhylaxGreen.copy(0.5f), PhylaxGreen, PhylaxGreen.copy(0.5f))
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedPercent,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(percent * 100).toInt()}%",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "USED",
                color = PhylaxGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (storage != null) "${formatSize(used)} / ${formatSize(total)}" else "Loading...",
                color = PhylaxGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CategoryStorageItem(info: CategoryInfo, totalSpace: Long) {
    val progress = (info.size.toFloat() / totalSpace.toFloat()).coerceIn(0f, 1f)
    
    val icon = when (info.name.lowercase()) {
        "images" -> Icons.Default.Image
        "videos" -> Icons.Default.PlayArrow
        "audio" -> Icons.Default.Audiotrack
        "documents" -> Icons.Default.Description
        "apk files" -> Icons.Default.Android
        "archives" -> Icons.Default.Folder
        else -> Icons.Default.InsertDriveFile
    }

    Surface(
        color = PhylaxCardBg.copy(0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.White.copy(0.05f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PhylaxGreen.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = PhylaxGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${info.count} files", color = PhylaxGray, fontSize = 11.sp)
                }
                Text(formatSize(info.size), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress * 5f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = PhylaxGreen,
                trackColor = Color.White.copy(0.05f)
            )
        }
    }
}

@Composable
fun StorageMetricsPanel(storage: StorageInfo?) {
    Surface(
        color = PhylaxCardBg.copy(0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            MetricRow("Internal Storage", formatSize(storage?.totalSpace ?: 0L), Icons.Default.SdStorage, PhylaxGreen)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(0.05f))
            MetricRow("Used Space", formatSize(storage?.usedSpace ?: 0L), Icons.Default.DataUsage, Color(0xFF00B0FF))
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(0.05f))
            MetricRow("Free Capacity", formatSize(storage?.freeSpace ?: 0L), Icons.Default.CloudQueue, Color(0xFFAA00FF))
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = PhylaxGray, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(PhylaxGreen.copy(0.3f), Color.Transparent)))
        )
    }
}
