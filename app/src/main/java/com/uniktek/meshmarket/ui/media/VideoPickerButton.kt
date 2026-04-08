package com.uniktek.meshmarket.ui.media

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uniktek.meshmarket.features.media.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Video picker button. Tap to pick from gallery, long-press to record with camera.
 * Compresses video before calling onVideoReady with the output path.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPickerButton(
    modifier: Modifier = Modifier,
    onVideoReady: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    fun processVideoUri(uri: Uri) {
        scope.launch {
            isProcessing = true
            try {
                // Check duration
                val durationMs = withContext(Dispatchers.IO) {
                    VideoUtils.getDurationMs(context, uri)
                }
                if (durationMs > VideoUtils.MAX_DURATION_MS) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(com.uniktek.meshmarket.R.string.video_too_long,
                                VideoUtils.formatDuration(VideoUtils.MAX_DURATION_MS)),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    isProcessing = false
                    return@launch
                }

                // Compress on IO thread
                val outPath = withContext(Dispatchers.IO) {
                    VideoUtils.compressForSending(context, uri)
                }
                if (!outPath.isNullOrBlank()) {
                    onVideoReady(outPath)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(com.uniktek.meshmarket.R.string.video_compress_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VideoPickerButton", "Failed to process video", e)
            } finally {
                isProcessing = false
            }
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processVideoUri(uri)
        }
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .combinedClickable(
                enabled = !isProcessing,
                onClick = { videoPicker.launch("video/*") },
                onLongClick = {
                    // Long-press also opens gallery for video (camera recording handled by system picker)
                    videoPicker.launch("video/*")
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Videocam,
            contentDescription = stringResource(com.uniktek.meshmarket.R.string.pick_video),
            tint = if (isProcessing) Color.DarkGray else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
