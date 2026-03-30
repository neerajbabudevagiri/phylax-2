package com.example.phylaxfileaccess.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.FileActivityEvent
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.models.SharingAppInfo
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import java.io.File
import java.util.UUID
import com.example.phylaxfileaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingAppsScreen(fileItem: FileItem?, onBack: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val viewModel: FileViewModel = viewModel(factory = FileViewModelFactory(context))
    
    // Log "OPEN_SHARE" when the screen is opened with a file
    LaunchedEffect(fileItem) {
        if (fileItem != null) {
            viewModel.logActivity(
                FileActivityEvent(
                    id = UUID.randomUUID().toString(),
                    filePath = fileItem.path,
                    timestamp = System.currentTimeMillis(),
                    eventType = "OPEN_SHARE"
                )
            )
        }
    }

    // Load BLOCKED apps from SharedPreferences
    val blockedAppPackages = remember(fileItem) {
        if (fileItem != null) {
            val prefs = context.getSharedPreferences("phylax_prefs", Context.MODE_PRIVATE)
            prefs.getStringSet("blocked_apps_${fileItem.path}", emptySet()) ?: emptySet()
        } else {
            emptySet()
        }
    }

    val sharingApps = remember(blockedAppPackages) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
        }
        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        resolveInfos.map { info ->
            SharingAppInfo(
                name = info.loadLabel(packageManager).toString(),
                icon = info.loadIcon(packageManager),
                packageName = info.activityInfo.packageName,
                activityName = info.activityInfo.name
            )
        }
        .filter { app ->
            !blockedAppPackages.contains(app.packageName)
        }
        .sortedBy { it.name }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PHYLAX SHARING",
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
            if (fileItem != null) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Sharing File:",
                        color = PhylaxGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = fileItem.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (blockedAppPackages.isNotEmpty()) {
                        Text(
                            text = "Some apps are restricted by Phylax Control",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
            }

            if (sharingApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No available apps for sharing.\nRestrictions may be active.",
                            color = PhylaxGray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onBack) {
                            Text("Adjust Share Control", color = PhylaxGreen)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sharingApps) { app ->
                        SharingAppListItem(app = app) {
                            if (fileItem != null) {
                                try {
                                    // Log "FILE_SHARED" when an app is selected
                                    viewModel.logActivity(
                                        FileActivityEvent(
                                            id = UUID.randomUUID().toString(),
                                            filePath = fileItem.path,
                                            timestamp = System.currentTimeMillis(),
                                            eventType = "FILE_SHARED",
                                            targetApp = app.name,
                                            targetPackage = app.packageName
                                        )
                                    )

                                    val file = File(fileItem.path)
                                    val uri: Uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    
                                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileItem.extension.lowercase()) ?: "*/*"
                                    
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = mimeType
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        setClassName(app.packageName, app.activityName)
                                    }
                                    context.startActivity(shareIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharingAppListItem(app: SharingAppInfo, onClick: () -> Unit) {
    Surface(
        color = PhylaxCardBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = PhylaxGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
