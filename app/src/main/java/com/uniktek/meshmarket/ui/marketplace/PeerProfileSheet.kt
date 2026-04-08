package com.uniktek.meshmarket.ui.marketplace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uniktek.meshmarket.R
import com.uniktek.meshmarket.core.ui.component.sheet.BitchatBottomSheet
import com.uniktek.meshmarket.model.ListingItem
import com.uniktek.meshmarket.ui.ChatViewModel
import com.uniktek.meshmarket.ui.theme.scaledFontSize
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerProfileSheet(
    peerID: String?,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onSendDM: (String) -> Unit
) {
    if (peerID == null) return

    val peerNicknames by viewModel.peerNicknames.collectAsStateWithLifecycle()
    val peerListings by viewModel.peerListings.collectAsStateWithLifecycle()
    val nickname = peerNicknames[peerID] ?: peerID.take(8)
    val listings = peerListings[peerID] ?: emptyList()

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red + colorScheme.background.green + colorScheme.background.blue < 1.5f

    BitchatBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Profile header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar circle with initial
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nickname.first().uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF007AFF)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nickname,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = peerID.take(16) + "...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }

                // DM button
                IconButton(onClick = { onSendDM(peerID); onDismiss() }) {
                    Icon(
                        Icons.Filled.Chat,
                        contentDescription = stringResource(R.string.send_message),
                        tint = Color(0xFF007AFF)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            // Listings section
            Text(
                text = stringResource(R.string.items_for_sale),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            if (listings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Storefront,
                            contentDescription = null,
                            tint = colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_items_from_peer),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listings, key = { it.id }) { listing ->
                        PeerListingCard(listing = listing, onContactSeller = {
                            onSendDM(peerID)
                            onDismiss()
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerListingCard(
    listing: ListingItem,
    onContactSeller: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listing.title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = scaledFontSize.sp,
                        color = colorScheme.onSurface
                    )
                    if (listing.price.isNotBlank()) {
                        Text(
                            text = listing.price,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF32D74B)
                        )
                    }
                }

                TextButton(
                    onClick = onContactSeller,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.contact_seller),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            if (listing.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = listing.description,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Image count hint (remote listings don't have local paths)
            val imageCount = listing.imagePaths.size
            if (imageCount > 0) {
                Spacer(Modifier.height(6.dp))
                // Show local images if they exist, otherwise show count badge
                val localImages = listing.imagePaths.filter { it.isNotBlank() && File(it).exists() }
                if (localImages.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(localImages) { path ->
                            val bmp = remember(path) { try { android.graphics.BitmapFactory.decodeFile(path) } catch (_: Exception) { null } }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Image,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.listing_image_count, imageCount),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
