package com.uniktek.meshmarket.ui.marketplace

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uniktek.meshmarket.R
import com.uniktek.meshmarket.core.ui.component.sheet.BitchatBottomSheet
import com.uniktek.meshmarket.model.ListingItem
import com.uniktek.meshmarket.ui.ChatViewModel
import com.uniktek.meshmarket.ui.theme.scaledFontSize
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsSheet(
    isPresented: Boolean,
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var editingListing by remember { mutableStateOf<ListingItem?>(null) }

    if (isPresented) {
        BitchatBottomSheet(onDismissRequest = {
            showCreateForm = false
            editingListing = null
            onDismiss()
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.my_listings),
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = {
                        editingListing = null
                        showCreateForm = true
                    }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_listing),
                            tint = Color(0xFF007AFF)
                        )
                    }
                }

                if (showCreateForm || editingListing != null) {
                    ListingForm(
                        existing = editingListing,
                        onSave = { listing ->
                            if (editingListing != null) {
                                viewModel.updateListing(listing)
                            } else {
                                viewModel.addListing(listing)
                            }
                            showCreateForm = false
                            editingListing = null
                        },
                        onCancel = {
                            showCreateForm = false
                            editingListing = null
                        }
                    )
                } else {
                    val listings = viewModel.getMyListings()
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
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_listings_yet),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.tap_plus_to_add),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listings, key = { it.id }) { listing ->
                                MyListingCard(
                                    listing = listing,
                                    onEdit = { editingListing = listing; showCreateForm = false },
                                    onDelete = { viewModel.removeListing(listing.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyListingCard(
    listing: ListingItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF32D74B)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFF007AFF), modifier = Modifier.size(22.dp))
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30), modifier = Modifier.size(22.dp))
                    }
                }
            }

            if (listing.description.isNotBlank()) {
                Text(
                    text = listing.description,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            // Image thumbnails
            if (listing.imagePaths.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listing.imagePaths) { path ->
                        if (path.isNotBlank() && File(path).exists()) {
                            val bmp = remember(path) { try { android.graphics.BitmapFactory.decodeFile(path) } catch (_: Exception) { null } }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_listing_title), fontFamily = FontFamily.Monospace) },
            text = { Text(stringResource(R.string.delete_listing_confirm), fontFamily = FontFamily.Monospace) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text(stringResource(R.string.delete_action), color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel_lower))
                }
            }
        )
    }
}

@Composable
private fun ListingForm(
    existing: ListingItem?,
    onSave: (ListingItem) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var price by remember { mutableStateOf(existing?.price ?: "") }
    var imagePaths by remember { mutableStateOf(existing?.imagePaths?.filter { it.isNotBlank() } ?: emptyList()) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && imagePaths.size < 5) {
            // Copy image to app storage
            try {
                val dir = File(context.filesDir, "listings/images").apply { mkdirs() }
                val outFile = File(dir, "listing_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
                imagePaths = imagePaths + outFile.absolutePath
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (existing != null) stringResource(R.string.edit_listing) else stringResource(R.string.new_listing),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = title,
            onValueChange = { if (it.length <= 80) title = it },
            label = { Text(stringResource(R.string.listing_title_hint), fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = price,
            onValueChange = { if (it.length <= 30) price = it },
            label = { Text(stringResource(R.string.listing_price_hint), fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 300) description = it },
            label = { Text(stringResource(R.string.listing_description_hint), fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // Image picker
        Text(
            text = stringResource(R.string.listing_images, imagePaths.size, 5),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(imagePaths) { path ->
                Box {
                    if (File(path).exists()) {
                        val bmp = remember(path) { try { android.graphics.BitmapFactory.decodeFile(path) } catch (_: Exception) { null } }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    IconButton(
                        onClick = { imagePaths = imagePaths - path },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
            if (imagePaths.size < 5) {
                item {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = stringResource(R.string.add_photo), tint = Color(0xFF007AFF))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.cancel_lower), fontFamily = FontFamily.Monospace)
            }
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ListingItem(
                                id = existing?.id ?: java.util.UUID.randomUUID().toString().uppercase(),
                                title = title.trim(),
                                description = description.trim(),
                                price = price.trim(),
                                imagePaths = imagePaths,
                                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                                sellerPeerID = existing?.sellerPeerID ?: "",
                                sellerNickname = existing?.sellerNickname ?: ""
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.save_listing), fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
