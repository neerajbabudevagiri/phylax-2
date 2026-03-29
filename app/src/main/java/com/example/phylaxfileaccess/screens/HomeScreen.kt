package com.example.phylaxfileaccess.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import com.example.phylaxfileaccess.utils.getFileIcon
import com.example.phylaxfileaccess.models.StorageInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.getValue
import java.io.File

// Theme Colors
val PhylaxGreen = Color(0xFF00FF7F)
val PhylaxBlack = Color(0xFF080808)
val PhylaxCardBg = Color(0xFF151515)
val PhylaxGray = Color(0xFF9E9E9E)
val PhylaxSurface = Color(0xFF1E1E1E)

// Squaricle shape constant
val SquaricleShape = RoundedCornerShape(28.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var showSharingApps by remember { mutableStateOf(false) }
    var shareControlFiles by remember { mutableStateOf<List<FileItem>?>(null) }
    var fileToShare by remember { mutableStateOf<FileItem?>(null) }
    var selectedActivityFile by remember { mutableStateOf<FileItem?>(null) }
    var selectedDetailsFile by remember { mutableStateOf<FileItem?>(null) }
    
    // Multi-file state
    var filesToShareMulti by remember { mutableStateOf<List<FileItem>?>(null) }

    val viewModel: FileViewModel = viewModel(
        factory = FileViewModelFactory(context)
    )

    val files by viewModel.recentFiles.collectAsState()
    val storage by viewModel.storage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFiles()
        viewModel.loadStorage()
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val gridState = rememberLazyGridState()
    val drawerScrollState = rememberScrollState()

    // Global launch state
    var isLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isLaunched = true
    }

    // Permanent scrollbar alpha
    val scrollbarAlpha = 0.4f

    if (selectedDetailsFile != null) {
        FileDetailsScreen(
            file = selectedDetailsFile!!,
            onBack = { selectedDetailsFile = null }
        )
        return
    }

    if (selectedActivityFile != null) {
        ActivityScreen(
            file = selectedActivityFile!!,
            onBack = { selectedActivityFile = null }
        )
        return
    }

    if (shareControlFiles != null) {
        ShareControlScreen(
            files = shareControlFiles!!,
            onBack = { shareControlFiles = null }
        )
        return
    }

    if (fileToShare != null) {
        SharingAppsScreen(
            fileItem = fileToShare,
            onBack = { fileToShare = null }
        )
        return
    }
    
    if (filesToShareMulti != null) {
        SharingAppsScreen(
            fileItem = filesToShareMulti!!.firstOrNull(), 
            onBack = { filesToShareMulti = null }
        )
        return
    }

    if (selectedFile != null) {
        FilePreviewScreen(
            file = selectedFile!!,
            onBack = { selectedFile = null }
        )
        return
    }

    if (showSharingApps) {
        SharingAppsScreen(
            fileItem = null,
            onBack = { showSharingApps = false }
        )
        return
    }

    if (selectedCategory != null) {
        CategoryScreen(
            category = selectedCategory!!,
            onBack = { selectedCategory = null },
            onFileClick = { file ->
                selectedFile = file
            },
            onEditClick = { file ->
                shareControlFiles = listOf(file)
            },
            onShareClick = { file ->
                fileToShare = file
            },
            onActivityClick = { file ->
                selectedActivityFile = file
            },
            onDetailsClick = { file ->
                selectedDetailsFile = file
            },
            onMultiShareClick = { files ->
                filesToShareMulti = files
                
                if (files.isNotEmpty()) {
                    val uris = ArrayList<Uri>()
                    files.forEach { fileItem ->
                        val file = File(fileItem.path)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        uris.add(uri)
                    }
                    
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "*/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share ${files.size} files"))
                }
            },
            onMultiLockClick = { files ->
                shareControlFiles = files
            }
        )
        return
    }

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PhylaxBlack,
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(drawerScrollState)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(PhylaxGreen.copy(0.2f), Color.Transparent)))
                                .border(1.dp, PhylaxGreen.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PhylaxGreen, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "PHYLAX",
                            color = PhylaxGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = 4.sp
                        )
                        Text(
                            "Administrator",
                            color = PhylaxGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = PhylaxSurface)
                    Spacer(Modifier.height(16.dp))

                    NavigationItem("Dashboard", Icons.Default.Dashboard, true) { scope.launch { drawerState.close() } }
                    
                    NavigationItem("File Permissions", Icons.Default.GppGood, false) {
                        scope.launch {
                            drawerState.close()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    intent.data = "package:${context.packageName}".toUri()
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }

                    NavigationItem("Control Access", Icons.Default.Lock, false) { scope.launch { drawerState.close() } }
                    NavigationItem("Sharing Apps", Icons.Default.Share, false) {
                        scope.launch { 
                            drawerState.close() 
                            showSharingApps = true
                        } 
                    }
                    NavigationItem("Recent Activity", Icons.Default.History, false) { scope.launch { drawerState.close() } }
                    NavigationItem("Settings", Icons.Default.Settings, false) { scope.launch { drawerState.close() } }
                    NavigationItem("About App", Icons.Default.Info, false) { scope.launch { drawerState.close() } }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "PHYLAX",
                            color = PhylaxGreen,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = PhylaxBlack
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { },
                    containerColor = PhylaxGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },
            containerColor = PhylaxBlack
        ) { paddingValues ->
            val categories = listOf(
                CategoryData("Images", "1,248 files", "2.3 GB", Icons.Default.Image),
                CategoryData("Videos", "86 files", "12.5 GB", Icons.Default.PlayArrow),
                CategoryData("Documents", "452 files", "1.2 GB", Icons.Default.Description),
                CategoryData("Audio", "320 files", "3.1 GB", Icons.Default.Audiotrack),
                CategoryData("APK Files", "15 files", "850 MB", Icons.Default.Android),
                CategoryData("Archives", "24 files", "5.4 GB", Icons.Default.Folder)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(if (isLandscape) 3 else 2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        MorphingStaggeredEntrance(index = 0, isLaunched = isLaunched) {
                            StorageOverviewCard(storage)
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StaggeredEntrance(index = 1, isLaunched = isLaunched) {
                            SectionHeader("Categories") {}
                        }
                    }

                    itemsIndexed(categories) { index, category ->
                        MorphingStaggeredEntrance(index = index + 2, isLaunched = isLaunched) {
                            CategoryCard(category) {
                                selectedCategory = category.name
                            }
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StaggeredEntrance(index = categories.size + 2, isLaunched = isLaunched) {
                            SectionHeader("Recent Files") {}
                        }
                    }

                    itemsIndexed(
                        items = files,
                        span = { _, _ -> GridItemSpan(maxLineSpan) }
                    ) { index, file ->
                        MorphingStaggeredEntrance(index = index + categories.size + 3, isLaunched = isLaunched) {
                            RecentFileListItem(
                                file = file, 
                                onEditClick = { shareControlFiles = listOf(it) },
                                onShareClick = { fileToShare = it },
                                onActivityClick = { selectedActivityFile = it },
                                onDetailsClick = { selectedDetailsFile = it }
                            )
                        }
                    }
                }

                // Permanent Scrollbar (Optimized with derivedStateOf)
                val scrollbarInfo by remember {
                    derivedStateOf {
                        val layoutInfo = gridState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val visibleItems = layoutInfo.visibleItemsInfo.size
                        if (totalItems > visibleItems) {
                            val firstVisible = gridState.firstVisibleItemIndex
                            val remaining = totalItems - firstVisible - visibleItems
                            Triple(firstVisible.toFloat() / totalItems, visibleItems.toFloat() / totalItems, remaining.toFloat() / totalItems)
                        } else null
                    }
                }

                scrollbarInfo?.let { (topWeight, thumbWeight, bottomWeight) ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(paddingValues)
                            .padding(end = 2.dp, top = 12.dp, bottom = 100.dp)
                            .width(4.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (topWeight > 0) Spacer(modifier = Modifier.weight(topWeight))
                            Box(
                                modifier = Modifier
                                    .weight(thumbWeight.coerceAtLeast(0.1f))
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .background(PhylaxGreen.copy(alpha = scrollbarAlpha))
                            )
                            if (bottomWeight > 0) Spacer(modifier = Modifier.weight(bottomWeight))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MorphingStaggeredEntrance(
    index: Int,
    isLaunched: Boolean,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (isLaunched) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = index * 100, easing = LinearOutSlowInEasing),
        label = "morphProgress"
    )

    val scale by animateFloatAsState(
        targetValue = if (isLaunched) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isLaunched) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = index * 100),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
                this.clip = true
                this.shape = getMorphingShape(progress)
            }
    ) {
        content()
    }
}

@Composable
fun StaggeredEntrance(
    index: Int,
    isLaunched: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isLaunched) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "staggeredScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isLaunched) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = index * 50),
        label = "staggeredAlpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.alpha = alpha
            }
    ) {
        content()
    }
}

fun getMorphingShape(progress: Float): Shape {
    return GenericShape { size, _ ->
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width.coerceAtMost(size.height) / 2f

        when {
            progress < 0.25f -> { // Circle to Triangle
                val p = progress / 0.25f
                drawPolygon(this, center, radius, 30, p, isCircleToTriangle = true)
            }
            progress < 0.5f -> { // Triangle to Star
                val p = (progress - 0.25f) / 0.25f
                drawTriangleToStar(this, center, radius, p)
            }
            progress < 0.75f -> { // Star to Square
                val p = (progress - 0.5f) / 0.25f
                drawStarToSquare(this, center, radius, p)
            }
            else -> { // Square to Squaricle
                val p = (progress - 0.75f) / 0.25f
                val cornerRadius = radius * 0.2f + (radius * 0.3f * p)
                addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), cornerRadius, cornerRadius))
            }
        }
    }
}

fun drawPolygon(path: Path, center: Offset, radius: Float, sides: Int, p: Float, isCircleToTriangle: Boolean) {
    val actualSides = if (isCircleToTriangle) {
        (30 - (30 - 3) * p).toInt().coerceAtLeast(3)
    } else sides

    val angleStep = 2.0 * PI / actualSides
    for (i in 0 until actualSides) {
        val angle = i * angleStep - PI / 2.0
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

fun drawTriangleToStar(path: Path, center: Offset, radius: Float, p: Float) {
    val sides = 5
    val angleStep = PI / sides
    for (i in 0 until 2 * sides) {
        val isInner = i % 2 != 0
        val r = if (isInner) radius * (0.4f + 0.1f * (1 - p)) else radius
        val angle = i * angleStep - PI / 2.0
        val x = center.x + r * cos(angle).toFloat()
        val y = center.y + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

fun drawStarToSquare(path: Path, center: Offset, radius: Float, p: Float) {
    val points = 10
    val angleStep = 2.0 * PI / points
    for (i in 0 until points) {
        val angle = i * angleStep - PI / 2.0
        val starR = if (i % 2 != 0) radius * 0.5f else radius
        val starX = center.x + starR * cos(angle).toFloat()
        val starY = center.y + starR * sin(angle).toFloat()

        // Square mapping (simplified interpolation)
        val sqX = center.x + radius * cos(angle).toFloat().coerceIn(-1f, 1f)
        val sqY = center.y + radius * sin(angle).toFloat().coerceIn(-1f, 1f)

        val x = starX + (sqX - starX) * p
        val y = starY + (sqY - starY) * p

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, PhylaxGreen.copy(0.3f), Color.Transparent)))
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all",
                color = PhylaxGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
    }
}

@Composable
fun NavigationItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PhylaxGreen.copy(alpha = 0.1f),
            selectedTextColor = PhylaxGreen,
            selectedIconColor = PhylaxGreen,
            unselectedTextColor = Color.White.copy(alpha = 0.7f),
            unselectedIconColor = Color.White.copy(alpha = 0.7f),
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun StorageOverviewCard(storage: StorageInfo?) {
    val used = storage?.usedSpace ?: 0L
    val total = storage?.totalSpace ?: 1L
    val percent = used.toFloat() / total.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = SquaricleShape,
                spotColor = PhylaxGreen
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(PhylaxGreen.copy(0.4f), Color.Transparent)),
                SquaricleShape
            ),
        colors = CardDefaults.cardColors(containerColor = PhylaxCardBg.copy(0.8f)),
        shape = SquaricleShape
    ) {
        Box(modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(PhylaxCardBg, Color(0xFF1A1A1A))
            )
        )) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = PhylaxGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Storage Usage",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (storage != null) "${formatSize(used)} used of ${formatSize(total)}" else "Loading...",
                        color = PhylaxGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        if (storage != null) "${(percent * 100).toInt()}%" else "--%",
                        color = PhylaxGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(0.05f), CircleShape),
                    color = PhylaxGreen,
                    trackColor = Color.White.copy(alpha = 0.05f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun CategoryCard(category: CategoryData, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .aspectRatio(0.9f)
            .shadow(8.dp, SquaricleShape)
            .clickable { onClick() }
            .border(0.5.dp, Color.White.copy(0.05f), SquaricleShape),
        colors = CardDefaults.cardColors(containerColor = PhylaxCardBg),
        shape = SquaricleShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PhylaxGreen.copy(alpha = alpha))
                    .border(1.dp, PhylaxGreen.copy(0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    tint = PhylaxGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    category.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        category.count,
                        color = PhylaxGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        " • ",
                        color = PhylaxGray,
                        fontSize = 11.sp
                    )
                    Text(
                        category.size,
                        color = PhylaxGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RecentFileListItem(
    file: FileItem, 
    onEditClick: (FileItem) -> Unit, 
    onShareClick: (FileItem) -> Unit,
    onActivityClick: (FileItem) -> Unit,
    onDetailsClick: (FileItem) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = PhylaxCardBg,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(0.5.dp, Color.White.copy(0.05f), RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            Uri.fromFile(java.io.File(file.path)),
                            "*/*"
                        )
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getFileIcon(file.extension),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        file.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (file.isDirectory) "Folder" else file.extension.uppercase(Locale.getDefault()),
                        color = PhylaxGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    formatSize(file.size),
                    color = PhylaxGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = PhylaxGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(PhylaxSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Details", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = PhylaxGreen) },
                        onClick = {
                            showMenu = false
                            onDetailsClick(file)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = PhylaxGreen) },
                        onClick = {
                            showMenu = false
                            onShareClick(file)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Activity", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = PhylaxGreen) },
                        onClick = {
                            showMenu = false
                            onActivityClick(file)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share Control", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PhylaxGreen) },
                        onClick = {
                            showMenu = false
                            onEditClick(file)
                        }
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}

data class CategoryData(
    val name: String,
    val count: String,
    val size: String,
    val icon: ImageVector
)
