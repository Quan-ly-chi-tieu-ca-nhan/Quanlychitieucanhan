package com.example.personalexpensemanagementapplication.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeStore {
    // default to system
    var currentTheme by mutableStateOf(AppTheme.System)
}

