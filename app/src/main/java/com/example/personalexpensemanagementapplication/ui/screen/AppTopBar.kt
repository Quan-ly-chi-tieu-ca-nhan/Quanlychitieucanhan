@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.personalexpensemanagementapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple TopBar with ONLY centered decorated title.
 * No back button, no notification, no actions.
 */
@Composable
fun AppTopBar(title: String) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            DecorativeTitleLarge(title = title)
        }
    }
}

/**
 * Title with rounded background highlight.
 */
@Composable
fun DecorativeTitleLarge(title: String) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}
