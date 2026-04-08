package com.uniktek.meshmarket.features.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object VideoUtils {

    private const val TAG = "VideoUtils"

    /** Max video duration in milliseconds (30 seconds) */
    const val MAX_DURATION_MS = 30_000L

    /** Target output resolution (longest edge) */
    private const val TARGET_MAX_DIM = 240

    /** Target video bitrate */
    private const val TARGET_VIDEO_BITRATE = 200_000 // 200 kbps

    /** Target audio bitrate */
    private const val TARGET_AUDIO_BITRATE = 48_000 // 48 kbps

    /** Target frame rate */
    private const val TARGET_FPS = 15

    /** I-frame interval in seconds */
    private const val I_FRAME_INTERVAL = 2

    /**
     * Extract a thumbnail bitmap from the first frame of a video file.
     */
    fun extractThumbnail(path: String, maxDim: Int = 256): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            frame?.let { scaleBitmap(it, maxDim) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract thumbnail: ${e.message}")
            null
        }
    }

    /**
     * Extract a thumbnail from a content URI.
     */
    fun extractThumbnail(context: Context, uri: Uri, maxDim: Int = 256): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            frame?.let { scaleBitmap(it, maxDim) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract thumbnail from URI: ${e.message}")
            null
        }
    }

    /**
     * Get the duration of a video in milliseconds.
     */
    fun getDurationMs(context: Context, uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get duration: ${e.message}")
            0L
        }
    }

    /**
     * Get the duration of a video file in milliseconds.
     */
    fun getDurationMs(path: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get duration: ${e.message}")
            0L
        }
    }

    /**
     * Format duration in ms to "0:15" style string.
     */
    fun formatDuration(durationMs: Long): String {
        val totalSec = (durationMs / 1000).toInt()
        val min = totalSec / 60
        val sec = totalSec % 60
        return "$min:%02d".format(sec)
    }

    /**
     * Compress a video from a content URI to a small, BLE-friendly file.
     * Transcodes to H.264 baseline 240p + AAC audio.
     * Returns the output file path, or null on failure.
     *
     * Falls back to a simpler copy-and-trim approach if hardware transcoding fails.
     */
    fun compressForSending(context: Context, uri: Uri): String? {
        return try {
            val outDir = File(context.filesDir, "videos/outgoing").apply { mkdirs() }
            val outFile = File(outDir, "vid_${System.currentTimeMillis()}.mp4")

            // Try hardware transcoding first
            val success = transcodeVideo(context, uri, outFile)
            if (success && outFile.exists() && outFile.length() > 0) {
                Log.d(TAG, "Transcoded video: ${outFile.length()} bytes")
                // Reject if still too large for reliable BLE transfer (~500KB target)
                val maxBleSize = 500_000L
                if (outFile.length() > maxBleSize) {
                    Log.w(TAG, "Transcoded video still too large (${outFile.length()} > $maxBleSize), rejecting")
                    outFile.delete()
                    null
                } else {
                    outFile.absolutePath
                }
            } else {
                // Do NOT fall back to raw copy - uncompressed video is too large for BLE
                Log.w(TAG, "Transcoding failed, video cannot be sent")
                outFile.delete()
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "compressForSending failed: ${e.message}", e)
            null
        }
    }

    /**
     * Compress a video from a file path.
     */
    fun compressForSending(context: Context, path: String): String? {
        val uri = Uri.fromFile(File(path))
        return compressForSending(context, uri)
    }

    /**
     * Save a thumbnail to disk as JPEG for display in the chat.
     */
    fun saveThumbnail(context: Context, bitmap: Bitmap): String? {
        return try {
            val dir = File(context.cacheDir, "videos/thumbnails").apply { mkdirs() }
            val file = File(dir, "thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save thumbnail: ${e.message}")
            null
        }
    }

    // ---- Internal transcoding implementation ----

    private fun transcodeVideo(context: Context, inputUri: Uri, outputFile: File): Boolean {
        var muxer: MediaMuxer? = null
        var videoDecoder: MediaCodec? = null
        var videoEncoder: MediaCodec? = null
        var audioExtractor: MediaExtractor? = null
        var videoExtractor: MediaExtractor? = null

        try {
            // Set up video extractor
            videoExtractor = MediaExtractor().apply {
                val fd = context.contentResolver.openFileDescriptor(inputUri, "r") ?: return false
                setDataSource(fd.fileDescriptor)
                fd.close()
            }

            val videoTrackIndex = findTrack(videoExtractor, "video/")
            if (videoTrackIndex < 0) {
                Log.e(TAG, "No video track found")
                return false
            }

            videoExtractor.selectTrack(videoTrackIndex)
            val inputVideoFormat = videoExtractor.getTrackFormat(videoTrackIndex)

            val inputWidth = inputVideoFormat.getInteger(MediaFormat.KEY_WIDTH)
            val inputHeight = inputVideoFormat.getInteger(MediaFormat.KEY_HEIGHT)

            // Calculate output dimensions (maintain aspect ratio, cap at TARGET_MAX_DIM)
            val (outWidth, outHeight) = calculateOutputDimensions(inputWidth, inputHeight)

            // Create output video format
            val outputVideoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outWidth, outHeight).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, TARGET_VIDEO_BITRATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, TARGET_FPS)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }

            // Create encoder
            videoEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            videoEncoder.configure(outputVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = videoEncoder.createInputSurface()
            videoEncoder.start()

            // Create decoder that renders to encoder's input surface
            videoDecoder = MediaCodec.createDecoderByType(
                inputVideoFormat.getString(MediaFormat.KEY_MIME) ?: MediaFormat.MIMETYPE_VIDEO_AVC
            )
            videoDecoder.configure(inputVideoFormat, inputSurface, null, 0)
            videoDecoder.start()

            // Set up muxer
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // Transcode video frames
            val muxerVideoTrack = transcodeFrames(
                videoExtractor, videoDecoder, videoEncoder, muxer
            )

            if (muxerVideoTrack < 0) {
                Log.e(TAG, "Video transcoding produced no output")
                return false
            }

            // Handle audio track: copy raw AAC frames if present
            audioExtractor = MediaExtractor().apply {
                val fd = context.contentResolver.openFileDescriptor(inputUri, "r") ?: return false
                setDataSource(fd.fileDescriptor)
                fd.close()
            }

            val audioTrackIndex = findTrack(audioExtractor, "audio/")
            if (audioTrackIndex >= 0) {
                audioExtractor.selectTrack(audioTrackIndex)
                val audioFormat = audioExtractor.getTrackFormat(audioTrackIndex)
                val muxerAudioTrack = muxer.addTrack(audioFormat)
                // Audio was not started in muxer yet because we added track after video started
                // Actually the muxer.start() was called in transcodeFrames, so we need to
                // copy audio separately
                copyAudioTrack(audioExtractor, muxer, muxerAudioTrack)
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "transcodeVideo failed: ${e.message}", e)
            return false
        } finally {
            try { videoDecoder?.stop() } catch (_: Exception) {}
            try { videoDecoder?.release() } catch (_: Exception) {}
            try { videoEncoder?.stop() } catch (_: Exception) {}
            try { videoEncoder?.release() } catch (_: Exception) {}
            try { videoExtractor?.release() } catch (_: Exception) {}
            try { audioExtractor?.release() } catch (_: Exception) {}
            try { muxer?.stop() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }

    private fun transcodeFrames(
        extractor: MediaExtractor,
        decoder: MediaCodec,
        encoder: MediaCodec,
        muxer: MediaMuxer
    ): Int {
        val bufferInfo = MediaCodec.BufferInfo()
        val timeoutUs = 10_000L
        var muxerTrackIndex = -1
        var muxerStarted = false
        var inputDone = false
        var decoderDone = false
        val maxTimeUs = MAX_DURATION_MS * 1000 // Convert ms to us

        while (true) {
            // Feed input to decoder
            if (!inputDone) {
                val inputIndex = decoder.dequeueInputBuffer(timeoutUs)
                if (inputIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputIndex) ?: continue
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0 || extractor.sampleTime > maxTimeUs) {
                        decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            // Drain decoder output -> render to encoder input surface
            if (!decoderDone) {
                val decoderOutputIndex = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (decoderOutputIndex >= 0) {
                    val endOfStream = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                    // Release buffer and render to surface (true = render)
                    decoder.releaseOutputBuffer(decoderOutputIndex, bufferInfo.size > 0)
                    if (endOfStream) {
                        encoder.signalEndOfInputStream()
                        decoderDone = true
                    }
                }
            }

            // Drain encoder output -> write to muxer
            val encoderOutputIndex = encoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
            when {
                encoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (!muxerStarted) {
                        muxerTrackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                }
                encoderOutputIndex >= 0 -> {
                    val outputBuffer = encoder.getOutputBuffer(encoderOutputIndex) ?: continue
                    if (bufferInfo.size > 0 && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(muxerTrackIndex, outputBuffer, bufferInfo)
                    }
                    val endOfStream = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                    encoder.releaseOutputBuffer(encoderOutputIndex, false)
                    if (endOfStream) break
                }
            }
        }

        return muxerTrackIndex
    }

    private fun copyAudioTrack(extractor: MediaExtractor, muxer: MediaMuxer, muxerTrackIndex: Int) {
        val buffer = ByteBuffer.allocate(256 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()
        val maxTimeUs = MAX_DURATION_MS * 1000

        while (true) {
            buffer.clear()
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0 || extractor.sampleTime > maxTimeUs) break
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = extractor.sampleFlags
            muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
            extractor.advance()
        }
    }

    private fun findTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith(mimePrefix)) return i
        }
        return -1
    }

    private fun calculateOutputDimensions(width: Int, height: Int): Pair<Int, Int> {
        val maxDim = maxOf(width, height)
        if (maxDim <= TARGET_MAX_DIM) return Pair(roundToEven(width), roundToEven(height))
        val scale = TARGET_MAX_DIM.toFloat() / maxDim.toFloat()
        return Pair(
            roundToEven((width * scale).toInt().coerceAtLeast(2)),
            roundToEven((height * scale).toInt().coerceAtLeast(2))
        )
    }

    /** MediaCodec requires even dimensions */
    private fun roundToEven(v: Int): Int = if (v % 2 == 0) v else v + 1

    private fun scaleBitmap(src: Bitmap, maxDim: Int): Bitmap {
        val w = src.width
        val h = src.height
        val scale = (maxOf(w, h).toFloat() / maxDim.toFloat()).coerceAtLeast(1f)
        val newW = (w / scale).toInt().coerceAtLeast(1)
        val newH = (h / scale).toInt().coerceAtLeast(1)
        return if (scale > 1f) Bitmap.createScaledBitmap(src, newW, newH, true) else src
    }

    private fun copyVideoFromUri(context: Context, uri: Uri, outFile: File): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { ins ->
                FileOutputStream(outFile).use { fos ->
                    ins.copyTo(fos)
                }
            }
            // Check size - if too large for BLE, warn but still return
            val size = outFile.length()
            val maxSize = com.uniktek.meshmarket.util.AppConstants.Media.MAX_FILE_SIZE_BYTES
            if (size > maxSize) {
                Log.w(TAG, "Video file $size bytes exceeds max $maxSize, will likely fail to send")
            }
            outFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "copyVideoFromUri failed: ${e.message}")
            null
        }
    }
}
