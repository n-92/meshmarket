package com.uniktek.meshmarket.model

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/**
 * A marketplace listing item that a user can advertise over the mesh.
 *
 * Stored locally as JSON. Broadcast as a compact binary payload so peers
 * can browse listings from nearby users. Thumbnails are embedded directly
 * in the payload so buyers can see preview images.
 */
data class ListingItem(
    val id: String = UUID.randomUUID().toString().uppercase(),
    val title: String,
    val description: String,
    val price: String,
    /** Local file paths for listing images (seller side) */
    val imagePaths: List<String> = emptyList(),
    /** Timestamp millis when the listing was created */
    val createdAt: Long = System.currentTimeMillis(),
    /** Seller's mesh peer ID (8-byte hex) */
    val sellerPeerID: String = "",
    /** Seller's nickname */
    val sellerNickname: String = "",
    /** Thumbnail JPEG bytes for each image (populated on receive side) */
    @Transient
    val thumbnails: List<ByteArray> = emptyList()
) {

    // ---- Binary TLV encoding for mesh broadcast ----
    //
    // TLV tags (1 byte tag, 2 byte length, variable value):
    //   0x01 = id (UTF-8)
    //   0x02 = title (UTF-8)
    //   0x03 = description (UTF-8)
    //   0x04 = price (UTF-8)
    //   0x05 = createdAt (8 bytes, big-endian)
    //   0x06 = sellerPeerID (UTF-8)
    //   0x07 = sellerNickname (UTF-8)
    //   0x08 = imageCount (1 byte)
    //   0x09 = thumbnail JPEG (bytes) — repeated, one per image

    /**
     * Encode listing with embedded thumbnails for mesh broadcast.
     * Call [encodeWithThumbnails] to generate thumbnails from local image paths.
     */
    fun encode(): ByteArray {
        val idB = id.toByteArray(Charsets.UTF_8)
        val titleB = title.toByteArray(Charsets.UTF_8)
        val descB = description.toByteArray(Charsets.UTF_8)
        val priceB = price.toByteArray(Charsets.UTF_8)
        val peerB = sellerPeerID.toByteArray(Charsets.UTF_8)
        val nickB = sellerNickname.toByteArray(Charsets.UTF_8)

        // Calculate capacity including thumbnails
        var thumbCapacity = 0
        for (thumb in thumbnails) {
            thumbCapacity += 1 + 2 + thumb.size // tag + 2-byte len + data
        }

        val capacity = (1 + 2 + idB.size) +
                (1 + 2 + titleB.size) +
                (1 + 2 + descB.size) +
                (1 + 2 + priceB.size) +
                (1 + 2 + 8) + // createdAt
                (1 + 2 + peerB.size) +
                (1 + 2 + nickB.size) +
                (1 + 2 + 1) + // imageCount
                thumbCapacity

        val buf = ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN)

        fun putTLV(tag: Byte, data: ByteArray) {
            buf.put(tag)
            buf.putShort(data.size.toShort())
            buf.put(data)
        }

        putTLV(0x01, idB)
        putTLV(0x02, titleB)
        putTLV(0x03, descB)
        putTLV(0x04, priceB)

        // createdAt
        buf.put(0x05.toByte())
        buf.putShort(8.toShort())
        buf.putLong(createdAt)

        putTLV(0x06, peerB)
        putTLV(0x07, nickB)

        // imageCount
        val imgCount = if (thumbnails.isNotEmpty()) thumbnails.size else imagePaths.size
        buf.put(0x08.toByte())
        buf.putShort(1.toShort())
        buf.put(imgCount.coerceAtMost(255).toByte())

        // Thumbnails
        for (thumb in thumbnails) {
            putTLV(0x09, thumb)
        }

        val result = ByteArray(buf.position())
        buf.rewind()
        buf.get(result)
        return result
    }

    companion object {
        /** Max thumbnail dimension in pixels */
        private const val THUMB_MAX_DIM = 120
        /** JPEG quality for thumbnails */
        private const val THUMB_QUALITY = 50

        fun decode(data: ByteArray): ListingItem? {
            try {
                var off = 0
                var id: String? = null
                var title: String? = null
                var description: String? = null
                var price: String? = null
                var createdAt: Long = 0L
                var sellerPeerID: String? = null
                var sellerNickname: String? = null
                var imageCount = 0
                val thumbs = mutableListOf<ByteArray>()

                while (off + 3 <= data.size) {
                    val tag = data[off].toInt() and 0xFF
                    off += 1
                    val len = ((data[off].toInt() and 0xFF) shl 8) or (data[off + 1].toInt() and 0xFF)
                    off += 2
                    if (off + len > data.size) return null
                    val value = data.copyOfRange(off, off + len)
                    off += len

                    when (tag) {
                        0x01 -> id = String(value, Charsets.UTF_8)
                        0x02 -> title = String(value, Charsets.UTF_8)
                        0x03 -> description = String(value, Charsets.UTF_8)
                        0x04 -> price = String(value, Charsets.UTF_8)
                        0x05 -> {
                            if (len == 8) {
                                val bb = ByteBuffer.wrap(value).order(ByteOrder.BIG_ENDIAN)
                                createdAt = bb.long
                            }
                        }
                        0x06 -> sellerPeerID = String(value, Charsets.UTF_8)
                        0x07 -> sellerNickname = String(value, Charsets.UTF_8)
                        0x08 -> {
                            if (len >= 1) imageCount = value[0].toInt() and 0xFF
                        }
                        0x09 -> thumbs.add(value)
                    }
                }

                return ListingItem(
                    id = id ?: return null,
                    title = title ?: return null,
                    description = description ?: "",
                    price = price ?: "",
                    createdAt = createdAt,
                    sellerPeerID = sellerPeerID ?: "",
                    sellerNickname = sellerNickname ?: "",
                    imagePaths = List(maxOf(imageCount, thumbs.size)) { "" },
                    thumbnails = thumbs
                )
            } catch (e: Exception) {
                return null
            }
        }

        /**
         * Generate small JPEG thumbnails from local image file paths.
         * Returns a new ListingItem with the thumbnails field populated.
         */
        fun withThumbnails(listing: ListingItem): ListingItem {
            val thumbs = mutableListOf<ByteArray>()
            for (path in listing.imagePaths) {
                if (path.isBlank()) continue
                try {
                    val file = java.io.File(path)
                    if (!file.exists()) continue
                    val bmp = android.graphics.BitmapFactory.decodeFile(path) ?: continue
                    val scaled = scaleBitmap(bmp, THUMB_MAX_DIM)
                    val baos = ByteArrayOutputStream()
                    scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, THUMB_QUALITY, baos)
                    thumbs.add(baos.toByteArray())
                    if (scaled !== bmp) scaled.recycle()
                    bmp.recycle()
                } catch (_: Exception) {}
            }
            return listing.copy(thumbnails = thumbs)
        }

        private fun scaleBitmap(src: android.graphics.Bitmap, maxDim: Int): android.graphics.Bitmap {
            val w = src.width
            val h = src.height
            val maxSide = maxOf(w, h)
            if (maxSide <= maxDim) return src
            val scale = maxDim.toFloat() / maxSide.toFloat()
            val newW = (w * scale).toInt().coerceAtLeast(1)
            val newH = (h * scale).toInt().coerceAtLeast(1)
            return android.graphics.Bitmap.createScaledBitmap(src, newW, newH, true)
        }
    }
}
