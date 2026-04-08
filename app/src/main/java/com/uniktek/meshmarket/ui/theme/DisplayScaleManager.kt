package com.uniktek.meshmarket.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Display scale options. Each multiplies the base font/icon sizes.
 */
enum class DisplayScale(val label: String, val factor: Float) {
    Small("S", 0.85f),
    Normal("M", 1.0f),
    Large("L", 1.2f),
    ExtraLarge("XL", 1.4f);
}

/**
 * SharedPreferences-backed manager for display scale with a StateFlow.
 */
object DisplayScaleManager {
    private const val PREFS_NAME = "bitchat_settings"
    private const val KEY_SCALE = "display_scale"

    private val _scaleFlow = MutableStateFlow(DisplayScale.Normal)
    val scaleFlow: StateFlow<DisplayScale> = _scaleFlow

    val factor: Float get() = _scaleFlow.value.factor

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SCALE, DisplayScale.Normal.name)
        _scaleFlow.value = runCatching { DisplayScale.valueOf(saved!!) }.getOrDefault(DisplayScale.Normal)
    }

    fun set(context: Context, scale: DisplayScale) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCALE, scale.name).apply()
        _scaleFlow.value = scale
    }
}
