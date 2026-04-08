package com.uniktek.meshmarket.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Vim-inspired color themes for MeshMarket.
 * Each entry maps to a classic vim colorscheme.
 */
enum class ColorTheme(
    val label: String,
    val isDark: Boolean
) {
    // -- Original bitchat themes --
    DEFAULT("default", true),
    LIGHT("light", false),

    // -- Classic vim built-in themes --
    BLUE("blue", true),
    DARKBLUE("darkblue", true),
    DELEK("delek", false),
    DESERT("desert", true),
    ELFLORD("elflord", true),
    EVENING("evening", true),
    HABAMAX("habamax", true),
    INDUSTRY("industry", true),
    KOEHLER("koehler", true),
    MORNING("morning", false),
    MURPHY("murphy", true),
    PABLO("pablo", true),
    PEACHPUFF("peachpuff", false),
    QUIET("quiet", false),
    RETROBOX("retrobox", true),
    RON("ron", true),
    SHINE("shine", false),
    SLATE("slate", true),
    SORBET("sorbet", true),
    TORTE("torte", true),
    WILDCHARM("wildcharm", true),
    ZAZEN("zazen", true),

    // -- Popular community themes --
    GRUVBOX("gruvbox", true),
    SOLARIZED_DARK("solarized dark", true),
    SOLARIZED_LIGHT("solarized light", false),
    DRACULA("dracula", true),
    MONOKAI("monokai", true),
    NORD("nord", true),
    CATPPUCCIN("catppuccin", true),
    ONEDARK("onedark", true),
    TOKYONIGHT("tokyonight", true);

    fun colorScheme(): ColorScheme = colorSchemeFor(this)
}

/**
 * SharedPreferences-backed manager for color theme with StateFlow.
 */
object ColorThemeManager {
    private const val PREFS_NAME = "bitchat_settings"
    private const val KEY_COLOR_THEME = "color_theme"

    private val _themeFlow = MutableStateFlow(ColorTheme.DEFAULT)
    val themeFlow: StateFlow<ColorTheme> = _themeFlow

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_COLOR_THEME, null)
        if (saved != null) {
            _themeFlow.value = runCatching { ColorTheme.valueOf(saved) }.getOrDefault(ColorTheme.DEFAULT)
        }
    }

    fun set(context: Context, theme: ColorTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()
        _themeFlow.value = theme
    }
}

// ── Color scheme definitions ────────────────────────────────────────

private fun colorSchemeFor(theme: ColorTheme): ColorScheme = when (theme) {

    // ── Original bitchat ──
    ColorTheme.DEFAULT -> darkColorScheme(
        primary = Color(0xFF39FF14),
        onPrimary = Color.Black,
        secondary = Color(0xFF2ECB10),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFF39FF14),
        surface = Color(0xFF111111),
        onSurface = Color(0xFF39FF14),
        error = Color(0xFFFF5555),
        onError = Color.Black
    )

    ColorTheme.LIGHT -> lightColorScheme(
        primary = Color(0xFF008000),
        onPrimary = Color.White,
        secondary = Color(0xFF006600),
        onSecondary = Color.White,
        background = Color.White,
        onBackground = Color(0xFF008000),
        surface = Color(0xFFF8F8F8),
        onSurface = Color(0xFF008000),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    // ── Vim built-in themes ──

    ColorTheme.BLUE -> darkColorScheme(
        primary = Color(0xFF00FFFF),
        onPrimary = Color(0xFF000080),
        secondary = Color(0xFFFFFF00),
        onSecondary = Color(0xFF000080),
        background = Color(0xFF000080),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF00006B),
        onSurface = Color(0xFFE0E0E0),
        error = Color(0xFFFF5555),
        onError = Color.White
    )

    ColorTheme.DARKBLUE -> darkColorScheme(
        primary = Color(0xFF00BFFF),
        onPrimary = Color(0xFF00002A),
        secondary = Color(0xFF87CEEB),
        onSecondary = Color(0xFF00002A),
        background = Color(0xFF00002A),
        onBackground = Color(0xFFD0D0D0),
        surface = Color(0xFF000040),
        onSurface = Color(0xFFC0C0C0),
        error = Color(0xFFFF6060),
        onError = Color.White
    )

    ColorTheme.DELEK -> lightColorScheme(
        primary = Color(0xFF0000FF),
        onPrimary = Color.White,
        secondary = Color(0xFF8B008B),
        onSecondary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFF0F0F0),
        onSurface = Color(0xFF000000),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    ColorTheme.DESERT -> darkColorScheme(
        primary = Color(0xFF87CEEB),
        onPrimary = Color(0xFF333333),
        secondary = Color(0xFFFFD700),
        onSecondary = Color(0xFF333333),
        background = Color(0xFF333333),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF3D3D3D),
        onSurface = Color(0xFFE0E0E0),
        error = Color(0xFFEE4444),
        onError = Color.White
    )

    ColorTheme.ELFLORD -> darkColorScheme(
        primary = Color(0xFF00FFFF),
        onPrimary = Color.Black,
        secondary = Color(0xFFFFFF00),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFF00FF00),
        surface = Color(0xFF1A1A1A),
        onSurface = Color(0xFF00FF00),
        error = Color(0xFFFF0000),
        onError = Color.White
    )

    ColorTheme.EVENING -> darkColorScheme(
        primary = Color(0xFF00FFFF),
        onPrimary = Color(0xFF00008B),
        secondary = Color(0xFFFFFF00),
        onSecondary = Color(0xFF00008B),
        background = Color(0xFF00008B),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF000070),
        onSurface = Color(0xFFE0E0FF),
        error = Color(0xFFFF5555),
        onError = Color.White
    )

    ColorTheme.HABAMAX -> darkColorScheme(
        primary = Color(0xFF9E9E9E),
        onPrimary = Color(0xFF1C1C1C),
        secondary = Color(0xFF6A9955),
        onSecondary = Color(0xFF1C1C1C),
        background = Color(0xFF1C1C1C),
        onBackground = Color(0xFFBCBCBC),
        surface = Color(0xFF262626),
        onSurface = Color(0xFFAAAAAA),
        error = Color(0xFFD16969),
        onError = Color.White
    )

    ColorTheme.INDUSTRY -> darkColorScheme(
        primary = Color(0xFF44FF44),
        onPrimary = Color(0xFF111111),
        secondary = Color(0xFFFFFF00),
        onSecondary = Color(0xFF111111),
        background = Color(0xFF111111),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF1A1A1A),
        onSurface = Color(0xFFE0E0E0),
        error = Color(0xFFFF0000),
        onError = Color.White
    )

    ColorTheme.KOEHLER -> darkColorScheme(
        primary = Color(0xFF00FFFF),
        onPrimary = Color.Black,
        secondary = Color(0xFFFFFF00),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF1A1A1A),
        onSurface = Color(0xFFFFFFFF),
        error = Color(0xFFFF0000),
        onError = Color.White
    )

    ColorTheme.MORNING -> lightColorScheme(
        primary = Color(0xFF006400),
        onPrimary = Color.White,
        secondary = Color(0xFF8B008B),
        onSecondary = Color.White,
        background = Color(0xFFF5F5F5),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF000000),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    ColorTheme.MURPHY -> darkColorScheme(
        primary = Color(0xFF00FF00),
        onPrimary = Color(0xFF004040),
        secondary = Color(0xFFFFFF00),
        onSecondary = Color(0xFF004040),
        background = Color(0xFF004040),
        onBackground = Color(0xFFC0FFC0),
        surface = Color(0xFF005050),
        onSurface = Color(0xFFB0E0B0),
        error = Color(0xFFFF5555),
        onError = Color.White
    )

    ColorTheme.PABLO -> darkColorScheme(
        primary = Color(0xFF00FF00),
        onPrimary = Color.Black,
        secondary = Color(0xFFADD8E6),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFFC0C0C0),
        surface = Color(0xFF1A1A1A),
        onSurface = Color(0xFFB0B0B0),
        error = Color(0xFFFF4444),
        onError = Color.White
    )

    ColorTheme.PEACHPUFF -> lightColorScheme(
        primary = Color(0xFF2E8B57),
        onPrimary = Color(0xFFFFDAB9),
        secondary = Color(0xFFCD853F),
        onSecondary = Color(0xFFFFDAB9),
        background = Color(0xFFFFDAB9),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFFFE4C4),
        onSurface = Color(0xFF2F2F2F),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    ColorTheme.QUIET -> lightColorScheme(
        primary = Color(0xFF555555),
        onPrimary = Color.White,
        secondary = Color(0xFF777777),
        onSecondary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFF5F5F5),
        onSurface = Color(0xFF333333),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    ColorTheme.RETROBOX -> darkColorScheme(
        primary = Color(0xFFFE8019),
        onPrimary = Color(0xFF1D2021),
        secondary = Color(0xFFB8BB26),
        onSecondary = Color(0xFF1D2021),
        background = Color(0xFF1D2021),
        onBackground = Color(0xFFEBDBB2),
        surface = Color(0xFF282828),
        onSurface = Color(0xFFD5C4A1),
        error = Color(0xFFFB4934),
        onError = Color.White
    )

    ColorTheme.RON -> darkColorScheme(
        primary = Color(0xFF00FFFF),
        onPrimary = Color.Black,
        secondary = Color(0xFFFF00FF),
        onSecondary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFF00FF00),
        surface = Color(0xFF0D0D0D),
        onSurface = Color(0xFF00FF00),
        error = Color(0xFFFF0000),
        onError = Color.White
    )

    ColorTheme.SHINE -> lightColorScheme(
        primary = Color(0xFF0000FF),
        onPrimary = Color.White,
        secondary = Color(0xFF006400),
        onSecondary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFF0F0F0),
        onSurface = Color(0xFF1A1A1A),
        error = Color(0xFFCC0000),
        onError = Color.White
    )

    ColorTheme.SLATE -> darkColorScheme(
        primary = Color(0xFF87CEEB),
        onPrimary = Color(0xFF262626),
        secondary = Color(0xFFF0E68C),
        onSecondary = Color(0xFF262626),
        background = Color(0xFF262626),
        onBackground = Color(0xFFD0D0D0),
        surface = Color(0xFF303030),
        onSurface = Color(0xFFC0C0C0),
        error = Color(0xFFEE4444),
        onError = Color.White
    )

    ColorTheme.SORBET -> darkColorScheme(
        primary = Color(0xFFFF79C6),
        onPrimary = Color(0xFF1E1E2E),
        secondary = Color(0xFFBD93F9),
        onSecondary = Color(0xFF1E1E2E),
        background = Color(0xFF1E1E2E),
        onBackground = Color(0xFFF8F8F2),
        surface = Color(0xFF282A36),
        onSurface = Color(0xFFE0E0E0),
        error = Color(0xFFFF5555),
        onError = Color.White
    )

    ColorTheme.TORTE -> darkColorScheme(
        primary = Color(0xFF90EE90),
        onPrimary = Color(0xFF1A1A2E),
        secondary = Color(0xFFEEEE00),
        onSecondary = Color(0xFF1A1A2E),
        background = Color(0xFF1A1A2E),
        onBackground = Color(0xFFCCCCCC),
        surface = Color(0xFF252540),
        onSurface = Color(0xFFBBBBBB),
        error = Color(0xFFFF4444),
        onError = Color.White
    )

    ColorTheme.WILDCHARM -> darkColorScheme(
        primary = Color(0xFFE0AF68),
        onPrimary = Color(0xFF1A1B26),
        secondary = Color(0xFF9ECE6A),
        onSecondary = Color(0xFF1A1B26),
        background = Color(0xFF1A1B26),
        onBackground = Color(0xFFA9B1D6),
        surface = Color(0xFF24283B),
        onSurface = Color(0xFF9AA5CE),
        error = Color(0xFFF7768E),
        onError = Color.White
    )

    ColorTheme.ZAZEN -> darkColorScheme(
        primary = Color(0xFF8FAA54),
        onPrimary = Color(0xFF191919),
        secondary = Color(0xFFC49060),
        onSecondary = Color(0xFF191919),
        background = Color(0xFF191919),
        onBackground = Color(0xFFA0A0A0),
        surface = Color(0xFF222222),
        onSurface = Color(0xFF909090),
        error = Color(0xFFCC6666),
        onError = Color.White
    )

    // ── Popular community themes ──

    ColorTheme.GRUVBOX -> darkColorScheme(
        primary = Color(0xFFFE8019),
        onPrimary = Color(0xFF282828),
        secondary = Color(0xFFB8BB26),
        onSecondary = Color(0xFF282828),
        background = Color(0xFF282828),
        onBackground = Color(0xFFEBDBB2),
        surface = Color(0xFF3C3836),
        onSurface = Color(0xFFD5C4A1),
        error = Color(0xFFFB4934),
        onError = Color.White
    )

    ColorTheme.SOLARIZED_DARK -> darkColorScheme(
        primary = Color(0xFF268BD2),
        onPrimary = Color(0xFF002B36),
        secondary = Color(0xFF2AA198),
        onSecondary = Color(0xFF002B36),
        background = Color(0xFF002B36),
        onBackground = Color(0xFF839496),
        surface = Color(0xFF073642),
        onSurface = Color(0xFF93A1A1),
        error = Color(0xFFDC322F),
        onError = Color.White
    )

    ColorTheme.SOLARIZED_LIGHT -> lightColorScheme(
        primary = Color(0xFF268BD2),
        onPrimary = Color(0xFFFDF6E3),
        secondary = Color(0xFF2AA198),
        onSecondary = Color(0xFFFDF6E3),
        background = Color(0xFFFDF6E3),
        onBackground = Color(0xFF657B83),
        surface = Color(0xFFEEE8D5),
        onSurface = Color(0xFF586E75),
        error = Color(0xFFDC322F),
        onError = Color.White
    )

    ColorTheme.DRACULA -> darkColorScheme(
        primary = Color(0xFFBD93F9),
        onPrimary = Color(0xFF282A36),
        secondary = Color(0xFF50FA7B),
        onSecondary = Color(0xFF282A36),
        background = Color(0xFF282A36),
        onBackground = Color(0xFFF8F8F2),
        surface = Color(0xFF44475A),
        onSurface = Color(0xFFF8F8F2),
        error = Color(0xFFFF5555),
        onError = Color.White
    )

    ColorTheme.MONOKAI -> darkColorScheme(
        primary = Color(0xFFA6E22E),
        onPrimary = Color(0xFF272822),
        secondary = Color(0xFFE6DB74),
        onSecondary = Color(0xFF272822),
        background = Color(0xFF272822),
        onBackground = Color(0xFFF8F8F2),
        surface = Color(0xFF3E3D32),
        onSurface = Color(0xFFF8F8F2),
        error = Color(0xFFF92672),
        onError = Color.White
    )

    ColorTheme.NORD -> darkColorScheme(
        primary = Color(0xFF88C0D0),
        onPrimary = Color(0xFF2E3440),
        secondary = Color(0xFF81A1C1),
        onSecondary = Color(0xFF2E3440),
        background = Color(0xFF2E3440),
        onBackground = Color(0xFFD8DEE9),
        surface = Color(0xFF3B4252),
        onSurface = Color(0xFFECEFF4),
        error = Color(0xFFBF616A),
        onError = Color.White
    )

    ColorTheme.CATPPUCCIN -> darkColorScheme(
        primary = Color(0xFFCBA6F7),
        onPrimary = Color(0xFF1E1E2E),
        secondary = Color(0xFFA6E3A1),
        onSecondary = Color(0xFF1E1E2E),
        background = Color(0xFF1E1E2E),
        onBackground = Color(0xFFCDD6F4),
        surface = Color(0xFF313244),
        onSurface = Color(0xFFBAC2DE),
        error = Color(0xFFF38BA8),
        onError = Color(0xFF1E1E2E)
    )

    ColorTheme.ONEDARK -> darkColorScheme(
        primary = Color(0xFF61AFEF),
        onPrimary = Color(0xFF282C34),
        secondary = Color(0xFF98C379),
        onSecondary = Color(0xFF282C34),
        background = Color(0xFF282C34),
        onBackground = Color(0xFFABB2BF),
        surface = Color(0xFF31353F),
        onSurface = Color(0xFFABB2BF),
        error = Color(0xFFE06C75),
        onError = Color.White
    )

    ColorTheme.TOKYONIGHT -> darkColorScheme(
        primary = Color(0xFF7AA2F7),
        onPrimary = Color(0xFF1A1B26),
        secondary = Color(0xFF9ECE6A),
        onSecondary = Color(0xFF1A1B26),
        background = Color(0xFF1A1B26),
        onBackground = Color(0xFFA9B1D6),
        surface = Color(0xFF24283B),
        onSurface = Color(0xFFC0CAF5),
        error = Color(0xFFF7768E),
        onError = Color.White
    )
}
