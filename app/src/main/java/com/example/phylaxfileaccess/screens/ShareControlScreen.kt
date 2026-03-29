package com.example.phylaxfileaccess.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsSuggest
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
fun ShareControlScreen(files: List<FileItem>, onBack: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val isBatchMode = files.size > 1
    
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
    val blockedApps = remember { 
        val map = mutableStateMapOf<String, Boolean>()
        
        if (!isBatchMode && files.isNotEmpty()) {
            // Load individual settings for this specific file
            val file = files.first()
            val saved = prefs.getStringSet("blocked_apps_${file.path}", null) ?: emptySet()
            sharingApps.forEach { app ->
                map[app.packageName] = saved.contains(app.packageName)
            }
        } else {
            // Batch mode: Start with current global/batch defaults if needed, 
            // but here we just clear to let user set new policy for the whole batch
            sharingApps.forEach { app ->
                map[app.packageName] = false
            }
        }
        map
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isBatchMode) "BATCH PROTECTION" else "FILE OVERRIDE",
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
                        val selectedPackages = blockedApps.filter { it.value }.keys.toSet()
                        
                        files.forEach { file ->
                            // Always save to the file-specific key to ensure this LATEST change 
                            // takes precedence for this file in the sharing screen.
                            prefs.edit().putStringSet("blocked_apps_${file.path}", selectedPackages).apply()
                        }
                        
                        val message = if (isBatchMode) "Batch settings applied" else "Individual override saved"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isBatchMode) PhylaxGreen else Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = if (isBatchMode) Color.Black else Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isBatchMode) "APPLY TO ALL" else "OVERRIDE FOR THIS FILE",
                        color = if (isBatchMode) Color.Black else Color.White,
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = PhylaxCardBg.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, PhylaxGreen.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBatchMode) Icons.Default.SettingsSuggest else Icons.Default.Block, 
                            contentDescription = null, 
                            tint = if (isBatchMode) PhylaxGreen else Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (isBatchMode) "LATEST BATCH CONFIG" else "LATEST FILE OVERRIDE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (isBatchMode) "Targeting: ${files.size} files" else "File: ${files.firstOrNull()?.name}",
                        color = PhylaxGray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isBatchMode) 
                            "Applying this selection will update the protection policy for every selected file." 
                            else "This setting is the most recent change and will override any previous batch policies for this file.",
                        color = PhylaxGray.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = "BLOCK APPS",
                color = PhylaxGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sharingApps) { app ->
                    val isChecked = blockedApps[app.packageName] ?: false
                    
                    Surface(
                        color = if (isChecked) PhylaxCardBg else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { blockedApps[app.packageName] = !isChecked },
                        border = if (isChecked) BorderStroke(1.dp, PhylaxGreen.copy(alpha = 0.2f)) else null
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
                                onCheckedChange = { blockedApps[app.packageName] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isBatchMode) PhylaxGreen else Color(0xFFD32F2F),
                                    uncheckedColor = PhylaxGray,
                                    checkmarkColor = if (isBatchMode) Color.Black else Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
