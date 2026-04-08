package com.uniktek.meshmarket.ui.media

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.ColorScheme
import com.uniktek.meshmarket.features.media.VideoUtils
import com.uniktek.meshmarket.mesh.BluetoothMeshService
import com.uniktek.meshmarket.model.BitchatMessage
import com.uniktek.meshmarket.model.DeliveryStatus
import java.io.File
import java.text.SimpleDateFormat

@Composable
fun VideoMessageItem(
    message: BitchatMessage,
    currentUserNickname: String,
    meshService: BluetoothMeshService,
    colorScheme: ColorScheme,
    timeFormatter: SimpleDateFormat,
    onNicknameClick: ((String) -> Unit)?,
    onMessageLongPress: ((BitchatMessage) -> Unit)?,
    onCancelTransfer: ((BitchatMessage) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val path = message.content.trim()
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header (sender, timestamp)
        val headerText = com.uniktek.meshmarket.ui.formatMessageHeaderAnnotatedString(
            message = message,
            currentUserNickname = currentUserNickname,
            meshService = meshService,
            colorScheme = colorScheme,
            timeFormatter = timeFormatter
        )
        val haptic = LocalHapticFeedback.current
        var headerLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
        Text(
            text = headerText,
            fontFamily = FontFamily.Monospace,
            color = colorScheme.onSurface,
            modifier = Modifier.pointerInput(message.id) {
                detectTapGestures(onTap = { pos ->
                    val layout = headerLayout ?: return@detectTapGestures
                    val offset = layout.getOffsetForPosition(pos)
                    val ann = headerText.getStringAnnotations("nickname_click", offset, offset)
                    if (ann.isNotEmpty() && onNicknameClick != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNicknameClick.invoke(ann.first().item)
                    }
                }, onLongPress = { onMessageLongPress?.invoke(message) })
            },
            onTextLayout = { headerLayout = it }
        )

        // Extract thumbnail
        val thumbnail = remember(path) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)
                val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                frame
            } catch (_: Exception) { null }
        }

        val durationText = remember(path) {
            val ms = VideoUtils.getDurationMs(path)
            if (ms > 0) VideoUtils.formatDuration(ms) else null
        }

        val progressFraction: Float? = when (val st = message.deliveryStatus) {
            is DeliveryStatus.PartiallyDelivered -> if (st.total > 0) st.reached.toFloat() / st.total.toFloat() else 0f
            else -> null
        }

        val fileExists = remember(path) { File(path).exists() }

        if (fileExists) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Box {
                    if (isPlaying) {
                        // Inline video player
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoURI(Uri.fromFile(File(path)))
                                    setOnCompletionListener {
                                        isPlaying = false
                                    }
                                    setOnPreparedListener { mp ->
                                        mp.start()
                                    }
                                    start()
                                }
                            },
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .aspectRatio(
                                    thumbnail?.let { it.width.toFloat() / it.height.toFloat() }
                                        ?.takeIf { it.isFinite() && it > 0 } ?: (16f / 9f)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isPlaying = false }
                        )
                    } else if (thumbnail != null) {
                        // Thumbnail with play button overlay
                        val img = thumbnail.asImageBitmap()
                        val aspect = (thumbnail.width.toFloat() / thumbnail.height.toFloat())
                            .takeIf { it.isFinite() && it > 0 } ?: (16f / 9f)

                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .aspectRatio(aspect)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (progressFraction == null || progressFraction >= 1f) {
                                        isPlaying = true
                                    }
                                }
                        ) {
                            if (progressFraction != null && progressFraction < 1f && message.sender == currentUserNickname) {
                                // Block reveal during transfer
                                BlockRevealImage(
                                    bitmap = img,
                                    progress = progressFraction,
                                    blocksX = 24,
                                    blocksY = 16,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    bitmap = img,
                                    contentDescription = stringResource(com.uniktek.meshmarket.R.string.cd_video),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Play button overlay (only when transfer complete)
                            if (progressFraction == null || progressFraction >= 1f) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = stringResource(com.uniktek.meshmarket.R.string.cd_play_video),
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            // Duration badge
                            if (durationText != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = durationText,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    } else {
                        // No thumbnail available - show placeholder
                        Box(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.DarkGray)
                                .clickable {
                                    if (progressFraction == null || progressFraction >= 1f) {
                                        isPlaying = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource(com.uniktek.meshmarket.R.string.cd_play_video),
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Cancel button during sending
                    val showCancel = message.sender == currentUserNickname &&
                            message.deliveryStatus is DeliveryStatus.PartiallyDelivered
                    if (showCancel) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .background(Color.Gray.copy(alpha = 0.6f), CircleShape)
                                .clickable { onCancelTransfer?.invoke(message) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(com.uniktek.meshmarket.R.string.cd_cancel),
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = stringResource(com.uniktek.meshmarket.R.string.video_unavailable),
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
        }
    }
}
