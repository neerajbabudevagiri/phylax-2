package com.example.phylaxfileaccess.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phylaxfileaccess.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    // Electric Neon Green for high visibility and brightness
    val electricGreen = Color(0xFF00FF7F)
    
    // Subtle gradient background colors
    val bgStart = Color(0xFF141414)
    val bgEnd = Color(0xFF080808)
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    
    // Adaptive thresholds
    val isSmallHeight = screenHeight < 500.dp
    val isCompact = screenWidth < 400.dp || isSmallHeight

    var startAnimation by remember { mutableStateOf(false) }
    val scanProgress = remember { Animatable(-0.2f) }

    LaunchedEffect(Unit) {
        delay(150)
        startAnimation = true
        // Professional scan pass
        scanProgress.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 3300, easing = LinearOutSlowInEasing)
        )
        delay(300)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(bgStart, bgEnd)
                )
            )
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "background_life")

        // --- 1. Ambient Background Atmosphere (Increased Brightness) ---
        repeat(6) { index ->
            val xOffsetAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 6000 + (index * 900), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "xOffset$index"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = (xOffsetAnim * size.width)
                val y = (size.height * (index / 6f))
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(electricGreen.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(x, y),
                        radius = 150f + (index * 20f)
                    ),
                    radius = 150f + (index * 20f),
                    center = Offset(x, y)
                )
            }
        }

        // --- 2. Triple Scanning Laser Beams ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = scanProgress.value
            val scanLines = listOf(
                progress to 0.6f,
                (progress - 0.06f) to 0.3f,
                (progress - 0.12f) to 0.15f
            )
            
            scanLines.forEach { (p, alpha) ->
                if (p in 0f..1f) {
                    val y = p * size.height
                    drawLine(
                        color = electricGreen.copy(alpha = alpha),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = (1.dp.toPx() * (alpha * 2))
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, electricGreen.copy(alpha = alpha * 0.15f), Color.Transparent),
                            startY = y - 60.dp.toPx(),
                            endY = y
                        ),
                        topLeft = Offset(0f, y - 60.dp.toPx()),
                        size = Size(size.width, 60.dp.toPx())
                    )
                }
            }
        }

        // --- 3. Responsive Content Layout ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            if (isLandscape) {
                // Landscape Layout: Side by Side
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    BrandingLogo(startAnimation, modifier = Modifier.weight(1f), isCompact = true)
                    Spacer(modifier = Modifier.width(32.dp))
                    BrandingContent(startAnimation, electricGreen, isCompact = true, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Start)
                }
            } else {
                // Portrait Layout: Stacked
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BrandingLogo(startAnimation, isCompact = isCompact)
                    Spacer(modifier = Modifier.height(if (isCompact) 16.dp else 24.dp))
                    BrandingContent(startAnimation, electricGreen, isCompact = isCompact, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 4. Responsive Branding Footer ---
            BrandingFooter(electricGreen, isCompact)
            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 32.dp))
        }
    }
}

@Composable
fun BrandingLogo(startAnimation: Boolean, modifier: Modifier = Modifier, isCompact: Boolean) {
    AnimatedVisibility(
        visible = startAnimation,
        enter = fadeIn(animationSpec = tween(750)) + 
                scaleIn(initialScale = 0.85f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.phylax_logo),
                contentDescription = "Phylax Logo",
                modifier = Modifier
                    .fillMaxWidth(if (isCompact) 0.5f else 0.65f)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
fun BrandingContent(
    startAnimation: Boolean, 
    electricGreen: Color, 
    isCompact: Boolean, 
    modifier: Modifier = Modifier,
    textAlign: TextAlign
) {
    val commonShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(2f, 2f),
        blurRadius = 4f
    )

    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // Welcome Message
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(750, delayMillis = 200)) + 
                    slideInVertically(initialOffsetY = { 30 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Text(
                text = "WELCOME TO",
                textAlign = textAlign,
                style = TextStyle(
                    fontSize = if (isCompact) 14.sp else 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = if (isCompact) 4.sp else 6.sp,
                    shadow = commonShadow
                )
            )
        }

        // Responsive Title
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(750, delayMillis = 400)) + 
                    slideInVertically(initialOffsetY = { 30 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Text(
                text = "PHYLAX",
                textAlign = textAlign,
                style = TextStyle(
                    fontSize = if (isCompact) 42.sp else 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = electricGreen,
                    letterSpacing = if (isCompact) 8.sp else 12.sp,
                    shadow = Shadow(
                        color = electricGreen.copy(alpha = 0.9f),
                        offset = Offset(0f, 0f),
                        blurRadius = if (isCompact) 30f else 45f
                    )
                )
            )
        }

        // Responsive Subtitle
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(750, delayMillis = 700)) +
                    slideInVertically(initialOffsetY = { 30 }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Text(
                text = "SECURE • TRACE • CONTROL",
                textAlign = textAlign,
                style = TextStyle(
                    fontSize = if (isCompact) 11.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = if (isCompact) 3.sp else 5.sp,
                    shadow = commonShadow
                ),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun BrandingFooter(electricGreen: Color, isCompact: Boolean) {
    val infiniteTransitionFooter = rememberInfiniteTransition(label = "footer_anim")
    val footerAlpha by infiniteTransitionFooter.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse),
        label = "footerAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f).height(0.5.dp).background(electricGreen.copy(alpha = footerAlpha * 0.4f)))
        Text(
            text = "@powered by Team Phylax - NITD",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = if (isCompact) 8.sp else 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = footerAlpha),
                letterSpacing = if (isCompact) 1.sp else 2.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset(1f, 1f),
                    blurRadius = 2f
                )
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(modifier = Modifier.weight(1f).height(0.5.dp).background(electricGreen.copy(alpha = footerAlpha * 0.4f)))
    }
}
