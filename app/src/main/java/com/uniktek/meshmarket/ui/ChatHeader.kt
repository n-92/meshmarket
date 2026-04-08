package com.uniktek.meshmarket.ui


import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.res.stringResource
import com.uniktek.meshmarket.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniktek.meshmarket.core.ui.utils.singleOrTripleClickable
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Header components for ChatScreen
 * Extracted from ChatScreen.kt for better organization
 */




@Composable
fun TorStatusDot(
    modifier: Modifier = Modifier
) {
    val torProvider = remember { com.uniktek.meshmarket.net.ArtiTorManager.getInstance() }
    val torStatus by torProvider.statusFlow.collectAsState()
    
    if (torStatus.mode != com.uniktek.meshmarket.net.TorMode.OFF) {
        val dotColor = when {
            torStatus.running && torStatus.bootstrapPercent < 100 -> Color(0xFFFF9500) // Orange - bootstrapping
            torStatus.running && torStatus.bootstrapPercent >= 100 -> Color(0xFF00C851) // Green - connected
            else -> Color.Red // Red - error/disconnected
        }
        Canvas(
            modifier = modifier
        ) {
            val radius = size.minDimension / 2
            drawCircle(
                color = dotColor,
                radius = radius,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
    }
}

@Composable
fun NoiseSessionIcon(
    sessionState: String?,
    modifier: Modifier = Modifier
) {
    val (icon, color, contentDescription) = when (sessionState) {
        "uninitialized" -> Triple(
            Icons.Outlined.NoEncryption,
            Color(0x87878700), // Grey - ready to establish
            stringResource(R.string.cd_ready_for_handshake)
        )
        "handshaking" -> Triple(
            Icons.Outlined.Sync,
            Color(0x87878700), // Grey - in progress
            stringResource(R.string.cd_handshake_in_progress)
        )
        "established" -> Triple(
            Icons.Filled.Lock,
            Color(0xFFFF9500), // Orange - secure
            stringResource(R.string.cd_encrypted)
        )
        else -> { // "failed" or any other state
            Triple(
                Icons.Outlined.Warning,
                Color(0xFFFF4444), // Red - error
                stringResource(R.string.cd_handshake_failed)
            )
        }
    }
    
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = color
    )
}

@Composable
fun NicknameEditor(
    value: String,
    onCommit: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var draft by remember(value) { mutableStateOf(value) }
    var isTaken by remember { mutableStateOf(false) }

    // Auto-scroll to end when text changes (simulates cursor following)
    LaunchedEffect(draft) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    // Clear error after a delay
    LaunchedEffect(isTaken) {
        if (isTaken) {
            kotlinx.coroutines.delay(2000)
            isTaken = false
        }
    }

    fun commitNickname() {
        val trimmed = draft.trim()
        if (trimmed.isNotEmpty() && trimmed != value) {
            val accepted = onCommit(trimmed)
            if (!accepted) {
                isTaken = true
                draft = value // revert to old nickname
            }
        }
        focusManager.clearFocus()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.at_symbol),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isTaken) colorScheme.error else colorScheme.primary.copy(alpha = 0.8f)
        )

        BasicTextField(
            value = draft,
            onValueChange = { draft = it; isTaken = false },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (isTaken) colorScheme.error else colorScheme.primary,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(if (isTaken) colorScheme.error else colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { commitNickname() }
            ),
            modifier = Modifier
                .widthIn(max = 120.dp)
                .horizontalScroll(scrollState)
        )

        if (isTaken) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "taken",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.error
            )
        }
    }
}

@Composable
fun PeerCounter(
    connectedPeers: List<String>,
    joinedChannels: Set<String>,
    hasUnreadChannels: Map<String, Int>,
    isConnected: Boolean,
    selectedLocationChannel: com.uniktek.meshmarket.geohash.ChannelID?,
    geohashPeople: List<GeoPerson>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Compute channel-aware people count and color (matches iOS logic exactly)
    val (peopleCount, countColor) = when (selectedLocationChannel) {
        is com.uniktek.meshmarket.geohash.ChannelID.Location -> {
            // Geohash channel: show geohash participants
            val count = geohashPeople.size
            val green = Color(0xFF00C851) // Standard green
            Pair(count, if (count > 0) green else Color.Gray)
        }
        is com.uniktek.meshmarket.geohash.ChannelID.Mesh,
        null -> {
            // Mesh channel: show Bluetooth-connected peers (excluding self)
            val count = connectedPeers.size
            val meshBlue = Color(0xFF007AFF) // iOS-style blue for mesh
            Pair(count, if (isConnected && count > 0) meshBlue else Color.Gray)
        }
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() }.padding(end = 8.dp) // Added right margin to match "bitchat" logo spacing
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = when (selectedLocationChannel) {
                is com.uniktek.meshmarket.geohash.ChannelID.Location -> stringResource(R.string.cd_geohash_participants)
                else -> stringResource(R.string.cd_connected_peers)
            },
            modifier = Modifier.size(20.dp),
            tint = countColor
        )
        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "$peopleCount",
            style = MaterialTheme.typography.bodyMedium,
            color = countColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (joinedChannels.isNotEmpty()) {
            Text(
                text = stringResource(R.string.channel_count_prefix) + "${joinedChannels.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConnected) Color(0xFF00C851) else Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatHeaderContent(
    selectedPrivatePeer: String?,
    currentChannel: String?,
    nickname: String,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onSidebarClick: () -> Unit,
    onTripleClick: () -> Unit,
    onShowAppInfo: () -> Unit,
    onLocationChannelsClick: () -> Unit,
    onLocationNotesClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    when {
        currentChannel != null -> {
            // Channel header
            ChannelHeader(
                channel = currentChannel,
                onBackClick = onBackClick,
                onLeaveChannel = { viewModel.leaveChannel(currentChannel) },
                onSidebarClick = onSidebarClick
            )
        }
        else -> {
            // Main header
            MainHeader(
                nickname = nickname,
                onNicknameCommit = viewModel::trySetNickname,
                onTitleClick = onShowAppInfo,
                onTripleTitleClick = onTripleClick,
                onSidebarClick = onSidebarClick,
                onLocationChannelsClick = onLocationChannelsClick,
                onLocationNotesClick = onLocationNotesClick,
                viewModel = viewModel
            )
        }
    }
}



@Composable
private fun ChannelHeader(
    channel: String,
    onBackClick: () -> Unit,
    onLeaveChannel: () -> Unit,
    onSidebarClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Box(modifier = Modifier.fillMaxWidth()) {
        // Back button - positioned all the way to the left with minimal margin
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp), // Reduced horizontal padding
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp) // Move even further left to minimize margin
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    modifier = Modifier.size(20.dp),
                    tint = colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.chat_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.primary
                )
            }
        }
        
        // Title - perfectly centered regardless of other elements
        Text(
            text = stringResource(R.string.chat_channel_prefix, channel),
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFFF9500), // Orange to match input field
            modifier = Modifier
                .align(Alignment.Center)
                .clickable { onSidebarClick() }
        )
        
        // Leave button - positioned on the right
        TextButton(
            onClick = onLeaveChannel,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                text = stringResource(R.string.chat_leave),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        }
    }
}

@Composable
private fun MainHeader(
    nickname: String,
    onNicknameCommit: (String) -> Boolean,
    onTitleClick: () -> Unit,
    onTripleTitleClick: () -> Unit,
    onSidebarClick: () -> Unit,
    onLocationChannelsClick: () -> Unit,
    onLocationNotesClick: () -> Unit,
    viewModel: ChatViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val connectedPeers by viewModel.connectedPeers.collectAsStateWithLifecycle()
    val joinedChannels by viewModel.joinedChannels.collectAsStateWithLifecycle()
    val hasUnreadChannels by viewModel.unreadChannelMessages.collectAsStateWithLifecycle()
    val hasUnreadPrivateMessages by viewModel.unreadPrivateMessages.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val selectedLocationChannel by viewModel.selectedLocationChannel.collectAsStateWithLifecycle()
    val geohashPeople by viewModel.geohashPeople.collectAsStateWithLifecycle()

    // Bookmarks store for current geohash toggle (iOS parity)
    val context = androidx.compose.ui.platform.LocalContext.current
    val bookmarksStore = remember { com.uniktek.meshmarket.geohash.GeohashBookmarksStore.getInstance(context) }
    val bookmarks by bookmarksStore.bookmarks.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.app_brand),
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.primary,
                modifier = Modifier.singleOrTripleClickable(
                    onSingleClick = onTitleClick,
                    onTripleClick = onTripleTitleClick
                )
            )
            
            Spacer(modifier = Modifier.width(2.dp))
            
            NicknameEditor(
                value = nickname,
                onCommit = onNicknameCommit
            )
        }
        
        // Right section with location channels button and peer counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {

            // Unread private messages badge (click to open most recent DM)
            if (hasUnreadPrivateMessages.isNotEmpty()) {
                // Render icon directly to avoid symbol resolution issues
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = stringResource(R.string.cd_unread_private_messages),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.openLatestUnreadPrivateChat() },
                    tint = Color(0xFFFF9500)
                )
            }

            // Location channels button (matching iOS implementation) and bookmark grouped tightly
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                LocationChannelsButton(
                    viewModel = viewModel,
                    onClick = onLocationChannelsClick
                )

                // Bookmark toggle for current geohash (not shown for mesh)
                val currentGeohash: String? = when (val sc = selectedLocationChannel) {
                    is com.uniktek.meshmarket.geohash.ChannelID.Location -> sc.channel.geohash
                    else -> null
                }
                if (currentGeohash != null) {
                    val isBookmarked = bookmarks.contains(currentGeohash)
                    Box(
                        modifier = Modifier
                            .padding(start = 2.dp) // minimal gap between geohash and bookmark
                            .size(20.dp)
                            .clickable { bookmarksStore.toggle(currentGeohash) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(R.string.cd_toggle_bookmark),
                            tint = if (isBookmarked) Color(0xFF00C851) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Location Notes button (extracted to separate component)
            LocationNotesButton(
                viewModel = viewModel,
                onClick = onLocationNotesClick
            )

            // Tor status dot when Tor is enabled
            TorStatusDot(
                modifier = Modifier
                    .size(8.dp)
                    .padding(start = 0.dp, end = 2.dp)
            )
            
            // PoW status indicator
            PoWStatusIndicator(
                modifier = Modifier,
                style = PoWIndicatorStyle.COMPACT
            )
            Spacer(modifier = Modifier.width(2.dp))
            // Animated refresh mesh button
            var isRefreshing by remember { mutableStateOf(false) }
            val rotation by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isRefreshing) 360f else 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 800,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                finishedListener = { isRefreshing = false },
                label = "refreshSpin"
            )
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.refresh_mesh),
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .clickable {
                        if (!isRefreshing) {
                            isRefreshing = true
                            viewModel.refreshMesh()
                        }
                    },
                tint = if (isRefreshing) Color(0xFF007AFF) else colorScheme.onSurface.copy(alpha = 0.6f)
            )
            PeerCounter(
                connectedPeers = connectedPeers.filter { it != viewModel.meshService.myPeerID },
                joinedChannels = joinedChannels,
                hasUnreadChannels = hasUnreadChannels,
                isConnected = isConnected,
                selectedLocationChannel = selectedLocationChannel,
                geohashPeople = geohashPeople,
                onClick = onSidebarClick
            )
        }
    }
}

@Composable
private fun LocationChannelsButton(
    viewModel: ChatViewModel,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Get current channel selection from location manager
    val selectedChannel by viewModel.selectedLocationChannel.collectAsStateWithLifecycle()
    val teleported by viewModel.isTeleported.collectAsStateWithLifecycle()
    
    val (badgeText, badgeColor) = when (selectedChannel) {
        is com.uniktek.meshmarket.geohash.ChannelID.Mesh -> {
            "#mesh" to Color(0xFF007AFF) // iOS blue for mesh
        }
        is com.uniktek.meshmarket.geohash.ChannelID.Location -> {
            val geohash = (selectedChannel as com.uniktek.meshmarket.geohash.ChannelID.Location).channel.geohash
            "#$geohash" to Color(0xFF00C851) // Green for location
        }
        null -> "#mesh" to Color(0xFF007AFF) // Default to mesh
    }
    
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = badgeColor
        ),
        contentPadding = PaddingValues(start = 4.dp, end = 0.dp, top = 2.dp, bottom = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = badgeColor,
                maxLines = 1
            )
            
            // Teleportation indicator (like iOS)
            if (teleported) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.PinDrop,
                    contentDescription = stringResource(R.string.cd_teleported),
                    modifier = Modifier.size(16.dp),
                    tint = badgeColor
                )
            }
        }
    }
}
