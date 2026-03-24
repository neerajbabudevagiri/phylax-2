package com.example.phylaxfileaccess.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.models.SharingAppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareControlScreen(file: FileItem, onBack: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val sharingApps = remember {
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
        }.sortedBy { it.name }
    }

    // Load initial state from SharedPreferences
    val prefs = remember { context.getSharedPreferences("phylax_prefs", Context.MODE_PRIVATE) }
    val allowedApps = remember { 
        val saved = prefs.getStringSet("allowed_apps_${file.path}", emptySet()) ?: emptySet()
        val map = mutableStateMapOf<String, Boolean>()
        sharingApps.forEach { app ->
            map[app.packageName] = saved.contains(app.packageName)
        }
        map
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SHARE CONTROL",
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
        bottomBar = {
            Surface(
                color = PhylaxBlack,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Button(
                    onClick = {
                        val selectedPackages = allowedApps.filter { it.value }.keys.toSet()
                        prefs.edit().putStringSet("allowed_apps_${file.path}", selectedPackages).apply()
                        Toast.makeText(context, "Sharing restrictions saved for ${file.name}", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "LOCK ACCESS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = PhylaxBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configuring sharing for:",
                    color = PhylaxGray,
                    fontSize = 12.sp
                )
                Text(
                    text = file.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                @Suppress("DEPRECATION")
                Text(
                    text = "Select apps allowed to share this file",
                    color = PhylaxGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sharingApps) { app ->
                    val isChecked = allowedApps[app.packageName] ?: false
                    
                    Surface(
                        color = PhylaxCardBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = app.icon.toBitmap().asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            @Suppress("DEPRECATION")
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.packageName,
                                    color = PhylaxGray,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { allowedApps[app.packageName] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2196F3), // Blue checkbox
                                    uncheckedColor = PhylaxGray,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
