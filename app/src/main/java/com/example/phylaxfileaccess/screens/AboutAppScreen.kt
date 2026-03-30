package com.example.phylaxfileaccess.screens

import android.os.Build
import android.os.Environment
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phylaxfileaccess.models.StorageInfo
import com.example.phylaxfileaccess.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    storage: StorageInfo?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Dynamic Permission Check
    val hasPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "USER GUIDE & STATUS",
                        color = PhylaxGreen,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PhylaxBlack)
            )
        },
        containerColor = PhylaxBlack
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TechBackgroundGrid()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TechHeader()

                Spacer(Modifier.height(32.dp))

                // App Description Section
                Surface(
                    color = PhylaxCardBg.copy(0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, PhylaxGreen.copy(0.2f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "WHAT IS PHYLAX?",
                            color = PhylaxGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Phylax is a high-security file management system designed to give you absolute control over your data. Monitor file access, organize sensitive documents into custom volumes, and share information through verified secure channels.",
                            color = Color.White.copy(0.8f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                SystemStatusPanel(storage, hasPermission)

                Spacer(Modifier.height(32.dp))

                Text(
                    "USER GUIDE & TIPS",
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                    color = PhylaxGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                
                Spacer(Modifier.height(16.dp))

                ProtocolItem(
                    index = "01",
                    title = "GETTING STARTED",
                    description = "To begin, go to 'File Permissions' in the side menu and enable 'All Files Access'. This allows Phylax to protect and manage your storage efficiently.",
                    icon = Icons.Default.Settings,
                    color = PhylaxGreen
                )

                ProtocolItem(
                    index = "02",
                    title = "FILE NAVIGATION",
                    description = "Easily find your files using the smart categories on your dashboard. Use the 'Recent Files' section to quickly pick up where you left off.",
                    icon = Icons.Default.FolderOpen,
                    color = Color(0xFF00B0FF)
                )

                ProtocolItem(
                    index = "03",
                    title = "CUSTOM CATEGORIES",
                    description = "Organize your workspace by creating custom categories. Tap the '+' button to group related files together for faster access.",
                    icon = Icons.Default.CreateNewFolder,
                    color = Color(0xFFAA00FF)
                )

                ProtocolItem(
                    index = "04",
                    title = "SECURITY LOGS",
                    description = "Keep track of your data's safety. Every time a file is accessed or shared, Phylax logs the activity so you can monitor for unauthorized use.",
                    icon = Icons.Default.SecurityUpdateGood,
                    color = Color(0xFFFF3D00)
                )

                ProtocolItem(
                    index = "05",
                    title = "SAFE SHARING",
                    description = "When sharing files, use our secure protocol to choose verified apps. This ensures your data remains protected even during transit.",
                    icon = Icons.Default.VpnLock,
                    color = Color(0xFFFFC107)
                )

                Spacer(Modifier.height(32.dp))

                UserQuickTipsPanel()

                Spacer(Modifier.height(48.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PHYLAX v1.0.4\nSECURE FILE MANAGEMENT SYSTEM",
                        color = PhylaxGreen.copy(0.4f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TechHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "headerPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(PhylaxGreen.copy(glowAlpha), Color.Transparent),
                            center = center,
                            radius = size.maxDimension / 1.5f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = PhylaxGreen,
                modifier = Modifier.size(70.dp)
            )
            
            val rotation = rememberInfiniteTransition(label = "ring").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )
            
            Canvas(modifier = Modifier.size(110.dp)) {
                drawArc(
                    color = PhylaxGreen,
                    startAngle = rotation.value,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawArc(
                    color = PhylaxGreen,
                    startAngle = rotation.value + 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "PHYLAX SYSTEM",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .background(PhylaxGreen.copy(0.1f), RoundedCornerShape(4.dp))
                .border(1.dp, PhylaxGreen.copy(0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Text(
                "VERIFIED USER ACCESS",
                color = PhylaxGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SystemStatusPanel(storage: StorageInfo?, hasPermission: Boolean) {
    val usedPercent = storage?.let { (it.usedSpace.toDouble() / it.totalSpace.toDouble() * 100).toInt() } ?: 0
    
    Surface(
        color = PhylaxCardBg.copy(0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(borderWithCorners(PhylaxGreen.copy(0.3f)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusLine("USER STATUS", "LOGGED IN", PhylaxGreen)
            StatusLine(
                "STORAGE CAPACITY", 
                if (storage != null) "$usedPercent% USED" else "SCANNING...", 
                if (usedPercent > 90) Color.Red else PhylaxGreen
            )
            StatusLine(
                "FILE PERMISSION", 
                if (hasPermission) "GRANTED" else "REQUIRED", 
                if (hasPermission) PhylaxGreen else Color(0xFFFF3D00)
            )
            StatusLine("APP VERSION", "1.0.4", Color(0xFFFFC107))
        }
    }
}

@Composable
fun StatusLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PhylaxGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ProtocolItem(index: String, title: String, description: String, icon: ImageVector, color: Color) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(if (expanded) borderWithCorners(color.copy(0.5f)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = PhylaxCardBg.copy(0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    index,
                    color = color.copy(0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(16.dp))
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = PhylaxGray
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        description,
                        color = PhylaxGray,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Brush.horizontalGradient(listOf(color.copy(0.5f), Color.Transparent)))
                    )
                }
            }
        }
    }
}

@Composable
fun UserQuickTipsPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(0.3f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Text(
            "QUICK ACTIONS SUMMARY",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(16.dp))
        TechSpecRow("FIND FILES", "Tap the search icon at the top of the home screen.")
        TechSpecRow("FILE DETAILS", "Tap the menu (⋮) on any file to see its history.")
        TechSpecRow("QUICK MENU", "Swipe from the left edge to open the navigation drawer.")
        TechSpecRow("STORAGE", "Check the home dashboard for your storage usage.")
    }
}

@Composable
fun TechSpecRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(">", color = PhylaxGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(8.dp))
        Text("$label:", color = PhylaxGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(4.dp))
        Text(value, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun TechBackgroundGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(
                color = Color.White.copy(0.03f),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(
                color = Color.White.copy(0.03f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

fun borderWithCorners(color: Color) = Modifier.drawBehind {
    val strokeWidth = 1.dp.toPx()
    val cornerLen = 20.dp.toPx()
    
    // Top Left
    drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth)
    drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokeWidth)
    
    // Top Right
    drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeWidth)
    drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeWidth)
    
    // Bottom Left
    drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), strokeWidth)
    drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeWidth)
    
    // Bottom Right
    drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeWidth)
    drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeWidth)
}
