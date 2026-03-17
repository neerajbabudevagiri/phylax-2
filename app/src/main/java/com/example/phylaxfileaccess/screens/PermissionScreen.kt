package com.example.phylaxfileaccess.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun PermissionScreen() {
    val context = LocalContext.current
    val phylaxGreen = Color(0xFF00FF7F)
    val bgStart = Color(0xFF141414)
    val bgEnd = Color(0xFF080808)
    val scrollState = rememberScrollState()

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
    ) {
        // Ambient Background
        val infiniteTransition = rememberInfiniteTransition(label = "ambient")
        repeat(3) { index ->
            val xOffsetAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000 + (index * 1200), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "xOffset$index"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = (xOffsetAnim * size.width)
                val y = (size.height * (index / 3f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(phylaxGreen.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(x, y),
                        radius = 200f + (index * 50f)
                    ),
                    radius = 200f + (index * 50f),
                    center = Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Icon with Glow
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { -40 })
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Outer Glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(30.dp, CircleShape, spotColor = phylaxGreen)
                            .background(phylaxGreen.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, phylaxGreen.copy(alpha = 0.3f), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.GppMaybe,
                        contentDescription = null,
                        tint = phylaxGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, 200)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                Text(
                    text = "AUTHORIZATION REQUIRED",
                    style = TextStyle(
                        color = phylaxGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        shadow = Shadow(phylaxGreen.copy(0.5f), blurRadius = 20f)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, 400)) + slideInVertically(initialOffsetY = { 20 })
            ) {
                Text(
                    text = "To ensure secure file tracing and control, Phylax requires 'All Files Access' permission. This allows the system to monitor and protect your sensitive data across the device storage.",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Button
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, 600)) + slideInVertically(initialOffsetY = { 40 })
            ) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.data = "package:${context.packageName}".toUri()
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = phylaxGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = phylaxGreen),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = "GRANT ACCESS",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer / Note
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, 800))
            ) {
                Text(
                    text = "You will be redirected to system settings",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}
