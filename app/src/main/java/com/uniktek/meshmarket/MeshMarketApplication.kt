package com.uniktek.meshmarket

import android.app.Application
import com.uniktek.meshmarket.nostr.RelayDirectory
import com.uniktek.meshmarket.ui.theme.ColorThemeManager
import com.uniktek.meshmarket.ui.theme.DisplayScaleManager
import com.uniktek.meshmarket.ui.theme.ThemePreferenceManager
import com.uniktek.meshmarket.net.ArtiTorManager

/**
 * Main application class for MeshMarket Android
 */
class MeshMarketApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Tor first so any early network goes over Tor
        try {
            val torProvider = ArtiTorManager.getInstance()
            torProvider.init(this)
        } catch (_: Exception){}

        // Initialize relay directory (loads assets/nostr_relays.csv)
        RelayDirectory.initialize(this)

        // Initialize LocationNotesManager dependencies early so sheet subscriptions can start immediately
        try { com.uniktek.meshmarket.nostr.LocationNotesInitializer.initialize(this) } catch (_: Exception) { }

        // Initialize favorites persistence early so MessageRouter/NostrTransport can use it on startup
        try {
            com.uniktek.meshmarket.favorites.FavoritesPersistenceService.initialize(this)
        } catch (_: Exception) { }

        // Warm up Nostr identity to ensure npub is available for favorite notifications
        try {
            com.uniktek.meshmarket.nostr.NostrIdentityBridge.getCurrentNostrIdentity(this)
        } catch (_: Exception) { }

        // Initialize theme and display scale preferences
        ThemePreferenceManager.init(this)
        DisplayScaleManager.init(this)
        ColorThemeManager.init(this)

        // Initialize debug preference manager (persists debug toggles)
        try { com.uniktek.meshmarket.ui.debug.DebugPreferenceManager.init(this) } catch (_: Exception) { }

        // Initialize Geohash Registries for persistence
        try {
            com.uniktek.meshmarket.nostr.GeohashAliasRegistry.initialize(this)
            com.uniktek.meshmarket.nostr.GeohashConversationRegistry.initialize(this)
        } catch (_: Exception) { }

        // Initialize mesh service preferences
        try { com.uniktek.meshmarket.service.MeshServicePreferences.init(this) } catch (_: Exception) { }

        // Proactively start the foreground service to keep mesh alive
        try { com.uniktek.meshmarket.service.MeshForegroundService.start(this) } catch (_: Exception) { }

        // TorManager already initialized above
    }
}
