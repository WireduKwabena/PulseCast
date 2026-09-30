package com.masterminds.pulsecast.ui.studio_vault_media_library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masterminds.pulsecast.core.MediaStoreMediaRepository
import com.masterminds.pulsecast.core.StorageTelemetry
import com.masterminds.pulsecast.core.VaultMediaItem
import com.masterminds.pulsecast.ui.theme.*
import java.util.Locale

@Composable
fun StudioVaultScreen(
    viewModel: StudioVaultViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNavigateToEditor: () -> Unit = {},
    onNavigateToClipExport: () -> Unit = {}
) {
    val context = LocalContext.current
    val mediaItems by viewModel.mediaItems.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedUris by viewModel.selectedUris.collectAsState()

    var isBatchActive by remember { mutableStateOf(false) }
    var isSearchTrayVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshMedia()
    }

    val filteredItems = remember(mediaItems, selectedCategory, searchQuery) {
        mediaItems.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "Recordings" -> !item.isClip
                "Clips" -> item.isClip
                else -> true
            }
            val matchesQuery = searchQuery.isEmpty() || item.title.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            VaultHeader(
                totalCount = mediaItems.size,
                onBack = onBack,
                onSearchToggle = { isSearchTrayVisible = !isSearchTrayVisible },
                isBatchActive = isBatchActive,
                onBatchToggle = { 
                    isBatchActive = !isBatchActive
                    if (!isBatchActive) viewModel.clearSelection()
                }
            )

            if (isSearchTrayVisible) {
                SearchTray(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { StorageTelemetryHUD(telemetry) }
                item { CategoryFilterStrip(selectedCategory, onCategorySelect = { viewModel.setCategory(it) }) }
                item { SortAndViewToggle() }

                if (filteredItems.isEmpty()) {
                    item {
                        Surface(
                            color = SurfaceLow,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.VideoLibrary, null, tint = OnSurfaceMuted, modifier = Modifier.size(36.dp))
                                Text("No Media Found in Vault", style = PulseCastType.headlineSm, color = OnSurface)
                                Text("Recordings and clips saved by PulseCast will appear here automatically.", style = PulseCastType.bodySm, color = OnSurfaceMuted, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(filteredItems) { item ->
                        val isSelected = selectedUris.contains(item.contentUri)
                        MediaCard(
                            item = item,
                            isSelected = isSelected,
                            isBatchActive = isBatchActive,
                            onToggleSelect = { viewModel.toggleSelection(item.contentUri) },
                            onPlay = { playVideo(context, item.contentUri) },
                            onShare = { shareVideo(context, item.contentUri) },
                            onTrimCut = onNavigateToEditor,
                            onMakeGif = onNavigateToClipExport,
                            onDelete = {
                                viewModel.toggleSelection(item.contentUri)
                                viewModel.deleteSelectedDirect()
                            }
                        )
                    }
                }
            }
        }

        // Floating Batch Bar
        AnimatedVisibility(
            visible = selectedUris.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        ) {
            BatchActionTray(
                selectedCount = selectedUris.size,
                onDelete = { viewModel.deleteSelectedDirect() },
                onCancel = { viewModel.clearSelection() }
            )
        }
    }
}

private fun playVideo(context: Context, contentUri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Play Video"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not play video", Toast.LENGTH_SHORT).show()
    }
}

private fun shareVideo(context: Context, contentUri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Video Clip"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share video", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun VaultHeader(totalCount: Int, onBack: () -> Unit, onSearchToggle: () -> Unit, isBatchActive: Boolean, onBatchToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = OnSurface)
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(PrimaryContainer).shadow(8.dp, CircleShape, spotColor = PrimaryContainer))
            Text("Studio Vault", style = PulseCastType.headlineLg.copy(fontSize = 24.sp))
            Surface(color = SurfaceHigh, shape = CircleShape) {
                Text("$totalCount Items", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onSearchToggle, modifier = Modifier.background(SurfaceLow, CircleShape)) {
                Icon(Icons.Default.Search, null, Modifier.size(20.dp), OnSurfaceMuted)
            }
            Surface(
                onClick = onBatchToggle,
                color = if (isBatchActive) PrimaryContainer else SurfaceLow,
                shape = CircleShape,
                modifier = Modifier.height(40.dp)
            ) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Checklist, null, Modifier.size(18.dp), if (isBatchActive) Color.Black else PrimaryContainer)
                    Text("Select", style = PulseCastType.buttonText, color = if (isBatchActive) Color.Black else OnSurface)
                }
            }
        }
    }
}

@Composable
private fun SearchTray(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    Surface(
        color = SurfaceLow,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Search, null, Modifier.size(20.dp), OnSurfaceMuted)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search clips by title or date...", style = PulseCastType.bodyMd, color = OnSurfaceMuted.copy(alpha = 0.6f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            )
        }
    }
}

@Composable
private fun StorageTelemetryHUD(telemetry: StorageTelemetry) {
    Surface(color = SurfaceLow, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Internal Broadcast Storage", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(String.format(Locale.US, "%.1f GB", telemetry.usedGb), style = PulseCastType.labelTelemetryLg, color = OnSurface)
                        Text(String.format(Locale.US, "/ %.0f GB", telemetry.totalGb), style = PulseCastType.labelTelemetryMd, color = OnSurfaceMuted)
                        Text(String.format(Locale.US, "(%.0f%% Used)", telemetry.usedPercentage * 100), style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(color = SurfaceHigh, shape = CircleShape) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Speed, null, Modifier.size(14.dp), NeonAmber)
                        Text("NVMe High-Speed", style = PulseCastType.labelTelemetrySm)
                    }
                }
            }
            
            // Multi-segmented bar
            Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(SurfaceHigh).padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.weight(telemetry.usedPercentage.coerceAtLeast(0.05f)).fillMaxHeight().background(PrimaryContainer, CircleShape))
                Box(Modifier.weight((1f - telemetry.usedPercentage).coerceAtLeast(0.05f)).fillMaxHeight().background(SurfaceHigh, CircleShape))
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StorageActionCard("Batch H.265", "Save Storage", Icons.Default.Compress, SecondaryFixedDim, Modifier.weight(1f))
                StorageActionCard("Purge Temp", "Clean Cache", Icons.Default.CleaningServices, NeonAmber, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StorageActionCard(label: String, sub: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(color = SurfaceHigh, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(32.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(18.dp), color)
            }
            Column {
                Text(label, style = PulseCastType.buttonText, fontSize = 11.sp)
                Text(sub, style = PulseCastType.labelTelemetrySm, color = color, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CategoryFilterStrip(selectedCategory: String, onCategorySelect: (String) -> Unit) {
    val categories = listOf("All", "Recordings", "Clips")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { cat ->
            val isSelected = selectedCategory == cat
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(cat) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isSelected) PrimaryContainer else SurfaceLow,
                    labelColor = if (isSelected) Color.Black else OnSurface
                )
            )
        }
    }
}

@Composable
private fun SortAndViewToggle() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Sorted by:", style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
            Text("Latest Captured", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ExpandMore, null, Modifier.size(14.dp), OnSurfaceMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.ViewAgenda, null, Modifier.size(18.dp), OnSurfaceMuted)
            Icon(Icons.Default.GridView, null, Modifier.size(18.dp), OnSurfaceMuted)
        }
    }
}

@Composable
private fun MediaCard(
    item: VaultMediaItem,
    isSelected: Boolean,
    isBatchActive: Boolean,
    onToggleSelect: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onTrimCut: () -> Unit,
    onMakeGif: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var thumbnailBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(item.contentUri) {
        val repo = MediaStoreMediaRepository(context)
        thumbnailBitmap = repo.loadThumbnail(item.contentUri)
    }

    Surface(color = SurfaceLow, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16/9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceMid)
                    .clickable { onPlay() }
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.VideoLibrary, null, Modifier.size(48.dp).align(Alignment.Center), OnSurfaceMuted.copy(alpha = 0.2f))
                }
                
                // Overlay Badges
                Row(Modifier.align(Alignment.TopStart).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = if (item.isClip) NeonAmber else PrimaryContainer, shape = RoundedCornerShape(4.dp)) {
                        Text(if (item.isClip) "CLIP" else "RAW REC", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp)) {
                        Text(item.resolution, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, color = OnSurface)
                    }
                }
                
                if (isBatchActive) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(28.dp).clickable { 
                            onToggleSelect()
                        },
                        color = if (isSelected) PrimaryContainer else Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape,
                        border = if (isSelected) null else BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        if (isSelected) Icon(Icons.Default.Check, null, Modifier.padding(4.dp), Color.Black)
                    }
                }
                
                // Play Button
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.Center).size(48.dp).clickable { onPlay() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(32.dp), Color.White)
                    }
                }
                
                Row(Modifier.align(Alignment.BottomStart).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp)) {
                        Text(item.formattedDuration, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = PulseCastType.labelTelemetrySm, fontWeight = FontWeight.Bold)
                    }
                    Text(item.formattedSize, style = PulseCastType.labelTelemetrySm, color = OnSurface.copy(alpha = 0.8f), modifier = Modifier.padding(vertical = 2.dp))
                }
            }
            
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(item.title, style = PulseCastType.headlineSm, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = OnSurfaceMuted)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Play Video") },
                                onClick = { showMenu = false; onPlay() },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Clip") },
                                onClick = { showMenu = false; onShare() },
                                leadingIcon = { Icon(Icons.Default.Share, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete File") },
                                onClick = { showMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = ElectricRuby) }
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.mimeType, style = PulseCastType.labelTelemetrySm, color = OnSurfaceMuted)
                    Text("•", color = OnSurfaceMuted)
                    Text(item.formattedDate, style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                }
            }
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).clickable { onTrimCut() }) {
                    Text("Trim / Cut", modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.buttonText, fontSize = 10.sp, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).clickable { onMakeGif() }) {
                    Text("Make GIF", modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.buttonText, fontSize = 10.sp, color = OnSurface)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).clickable { onShare() }) {
                    Text("Share", modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.buttonText, fontSize = 10.sp, color = SecondaryFixedDim, fontWeight = FontWeight.Bold)
                }
                Surface(color = SurfaceHigh, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).clickable { onShare() }) {
                    Text("Upload", modifier = Modifier.padding(vertical = 8.dp), textAlign = TextAlign.Center, style = PulseCastType.buttonText, fontSize = 10.sp, color = PrimaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BatchActionTray(selectedCount: Int, onDelete: () -> Unit, onCancel: () -> Unit) {
    Surface(
        color = BgBase.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shadowElevation = 24.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = PrimaryContainer, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(selectedCount.toString(), style = PulseCastType.labelTelemetryMd, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Text("Items Selected", style = PulseCastType.buttonText)
                    Text("Ready for batch operation", style = PulseCastType.labelTelemetrySm, color = SecondaryFixedDim)
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDelete, modifier = Modifier.background(ElectricRuby.copy(alpha = 0.2f), RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Delete, null, Modifier.size(20.dp), ElectricRuby)
                }
                IconButton(onClick = onCancel, modifier = Modifier.background(SurfaceHigh, RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Close, null, Modifier.size(20.dp), OnSurface)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0D14)
@Composable
fun StudioVaultPreview() {
    PulseCastTheme {
        StudioVaultScreen()
    }
}
