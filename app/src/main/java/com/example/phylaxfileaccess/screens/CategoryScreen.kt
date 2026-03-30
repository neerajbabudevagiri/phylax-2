package com.example.phylaxfileaccess.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.phylaxfileaccess.models.FileItem
import com.example.phylaxfileaccess.utils.getFileIcon
import com.example.phylaxfileaccess.viewmodel.FileViewModel
import com.example.phylaxfileaccess.viewmodel.FileViewModelFactory
import java.util.Locale
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.BorderStroke
import com.example.phylaxfileaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    category: String,
    onBack: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onEditClick: (FileItem) -> Unit,
    onShareClick: (FileItem) -> Unit,
    onActivityClick: (FileItem) -> Unit,
    onDetailsClick: (FileItem) -> Unit,
    onMultiShareClick: (List<FileItem>) -> Unit = {},
    onMultiLockClick: (List<FileItem>) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: FileViewModel = viewModel(
        factory = FileViewModelFactory(context)
    )
    
    val files by viewModel.categoryFiles.collectAsState()
    var selectedFiles by remember { mutableStateOf(setOf<FileItem>()) }
    val isSelectionMode = selectedFiles.isNotEmpty()

    LaunchedEffect(category) {
        viewModel.loadFilesByCategory(category)
        selectedFiles = emptySet()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedFiles.size} SELECTED" else category.uppercase(),
                        color = if (isSelectionMode) Color.White else PhylaxGreen,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            selectedFiles = emptySet()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            onMultiShareClick(selectedFiles.toList())
                            selectedFiles = emptySet()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share All", tint = PhylaxGreen)
                        }
                        IconButton(onClick = { 
                            onMultiLockClick(selectedFiles.toList())
                            selectedFiles = emptySet()
                        }) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock All", tint = PhylaxGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isSelectionMode) PhylaxGreen.copy(alpha = 0.2f) else PhylaxBlack
                )
            )
        },
        containerColor = PhylaxBlack,
        floatingActionButton = {
            if (isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = { 
                        if (selectedFiles.size == files.size) selectedFiles = emptySet()
                        else selectedFiles = files.toSet()
                    },
                    containerColor = PhylaxGreen,
                    contentColor = Color.Black,
                    icon = { Icon(if (selectedFiles.size == files.size) Icons.Default.Deselect else Icons.Default.SelectAll, null) },
                    text = { Text(if (selectedFiles.size == files.size) "Deselect All" else "Select All") }
                )
            }
        }
    ) { padding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No files found in $category", color = PhylaxGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files) { file ->
                    val isSelected = selectedFiles.contains(file)
                    CategoryFileItem(
                        file = file, 
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                selectedFiles = if (isSelected) selectedFiles - file else selectedFiles + file
                            } else {
                                onFileClick(file)
                            }
                        },
                        onLongClick = {
                            if (!isSelectionMode) {
                                selectedFiles = setOf(file)
                            }
                        },
                        onEditClick = { onEditClick(file) },
                        onShareClick = { onShareClick(file) },
                        onActivityClick = { onActivityClick(file) },
                        onDetailsClick = { onDetailsClick(file) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CategoryFileItem(
    file: FileItem, 
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit, 
    onLongClick: () -> Unit,
    onEditClick: () -> Unit, 
    onShareClick: () -> Unit,
    onActivityClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = if (isSelected) PhylaxGreen.copy(alpha = 0.15f) else PhylaxCardBg,
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(1.dp, PhylaxGreen) else null,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                    .background(if (isSelected) PhylaxGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = PhylaxGreen)
                } else {
                    Icon(
                        imageVector = getFileIcon(file.extension),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    file.extension.uppercase(Locale.getDefault()),
                    color = PhylaxGray,
                    fontSize = 12.sp
                )
            }
            
            if (!isSelectionMode) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = PhylaxGreen,
                            modifier = Modifier.size(24.dp)
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
                                onDetailsClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = PhylaxGreen) },
                            onClick = {
                                showMenu = false
                                onShareClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Activity", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = PhylaxGreen) },
                            onClick = {
                                showMenu = false
                                onActivityClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Control", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PhylaxGreen) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                    }
                }
            } else {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PhylaxGreen,
                        uncheckedColor = PhylaxGray,
                        checkmarkColor = Color.Black
                    )
                )
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
