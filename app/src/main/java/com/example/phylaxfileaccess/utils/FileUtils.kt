package com.example.phylaxfileaccess.utils

import kotlin.math.log
import kotlin.math.pow

fun formatSize(bytes: Long): String {

    if (bytes < 1024) return "$bytes B"

    val exp = (log(bytes.toDouble(),1024.0)).toInt()
    val pre = "KMGTPE"[exp-1]

    return String.format("%.1f %sB", bytes/1024.0.pow(exp.toDouble()), pre)
}