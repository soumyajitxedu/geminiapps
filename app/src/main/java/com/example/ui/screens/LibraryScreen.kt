package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ResourceEntity
import com.example.ui.MainViewModel
import com.example.ui.components.PixelArtEmblem
import com.example.ui.theme.PixelifySansFontFamily

private val LIBRARY_SUBJECTS = listOf(
    "All",
    "General",
    "Physics",
    "Chemistry",
    "Maths",
    "Biology",
    "English",
    "History",
    "Geography",
    "Computer Applications",
    "L2"
)

private val ADD_SUBJECT_OPTIONS = listOf(
    "General",
    "Physics",
    "Chemistry",
    "Maths",
    "Biology",
    "English",
    "History",
    "Geography",
    "Computer Applications",
    "L2"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedSubject by viewModel.selectedLibrarySubject.collectAsStateWithLifecycle()
    val searchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle()
    val resources by viewModel.filteredResources.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingResource by remember { mutableStateOf<ResourceEntity?>(null) }
    var selectedResourceAction by remember { mutableStateOf<ResourceEntity?>(null) }
    var resourceToDelete by remember { mutableStateOf<ResourceEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_resource")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Resource")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Banner with Pixel Style
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelArtEmblem(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Study Inventory",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = PixelifySansFontFamily,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "ICSE Books, Past Papers & Personal Vault",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Count Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "${resources.size} items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setLibrarySearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("library_search_input"),
                        placeholder = { Text("Search by title, subject or book name...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setLibrarySearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal FilterChips for Subjects
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LIBRARY_SUBJECTS.forEach { subject ->
                            val isSelected = selectedSubject.equals(subject, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setLibrarySubject(subject) },
                                label = {
                                    Text(
                                        text = subject,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_$subject")
                            )
                        }
                    }
                }
            }

            // Resources List
            if (resources.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Resources Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = PixelifySansFontFamily
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\" in $selectedSubject." else "Tap '+' below to add your first study resource!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(resources, key = { it.id }) { resource ->
                        InventoryResourceCard(
                            resource = resource,
                            onClick = {
                                openResourceUrl(context, resource.url)
                            },
                            onLongClick = {
                                selectedResourceAction = resource
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add Resource Dialog
    if (showAddDialog) {
        ResourceFormDialog(
            title = "Add Study Resource",
            initialTitle = "",
            initialSubject = if (selectedSubject != "All") selectedSubject else "Physics",
            initialUrl = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, subject, url ->
                viewModel.addCustomResource(title, subject, url)
                showAddDialog = false
                Toast.makeText(context, "Resource added to Study Inventory!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Resource Dialog
    editingResource?.let { res ->
        ResourceFormDialog(
            title = "Edit Study Resource",
            initialTitle = res.title,
            initialSubject = res.subject,
            initialUrl = res.url,
            onDismiss = { editingResource = null },
            onConfirm = { title, subject, url ->
                viewModel.updateCustomResource(res.copy(title = title, subject = subject, url = url))
                editingResource = null
                Toast.makeText(context, "Resource updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Long-Press Action Modal (Edit, Delete, Open)
    selectedResourceAction?.let { res ->
        AlertDialog(
            onDismissRequest = { selectedResourceAction = null },
            title = {
                Text(
                    text = "Manage Resource",
                    fontWeight = FontWeight.Bold,
                    fontFamily = PixelifySansFontFamily
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = res.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Subject: ${res.subject}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val target = res
                            selectedResourceAction = null
                            editingResource = target
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    Button(
                        onClick = {
                            val target = res
                            selectedResourceAction = null
                            resourceToDelete = target
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedResourceAction = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    resourceToDelete?.let { res ->
        AlertDialog(
            onDismissRequest = { resourceToDelete = null },
            title = { Text("Delete Resource?") },
            text = { Text("Are you sure you want to remove \"${res.title}\" from your Study Inventory?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteResource(res)
                        resourceToDelete = null
                        Toast.makeText(context, "Resource deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { resourceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryResourceCard(
    resource: ResourceEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val subjectColor = getSubjectColor(resource.subject)
    val fileType = detectFileType(resource.title, resource.url)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("resource_card_${resource.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Document Icon with Subject Background & File Type Icon
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = subjectColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = fileType.icon,
                        contentDescription = fileType.label,
                        tint = subjectColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subject Tag
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, subjectColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = resource.subject,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = fileType.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (resource.isUserAdded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Personal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Arrow
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open resource",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class FileTypeInfo(val label: String, val icon: ImageVector)

private fun detectFileType(title: String, url: String): FileTypeInfo {
    val lowerTitle = title.lowercase()
    val lowerUrl = url.lowercase()

    return when {
        lowerTitle.contains("pdf") || lowerUrl.endsWith(".pdf") || lowerUrl.contains("/pdf") -> {
            FileTypeInfo("PDF", Icons.Default.PictureAsPdf)
        }
        lowerUrl.contains("drive.google.com") -> {
            FileTypeInfo("Drive", Icons.Default.Description)
        }
        lowerUrl.isNotBlank() -> {
            FileTypeInfo("Link", Icons.Default.Link)
        }
        else -> {
            FileTypeInfo("Doc", Icons.Default.Description)
        }
    }
}

private fun openResourceUrl(context: android.content.Context, url: String) {
    if (url.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "No URL link available for this file", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceFormDialog(
    title: String,
    initialTitle: String,
    initialSubject: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, subject: String, url: String) -> Unit
) {
    var resourceTitle by remember { mutableStateOf(initialTitle) }
    var resourceSubject by remember { mutableStateOf(initialSubject) }
    var resourceUrl by remember { mutableStateOf(initialUrl) }
    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelifySansFontFamily
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = resourceTitle,
                    onValueChange = { resourceTitle = it },
                    label = { Text("Resource Title") },
                    placeholder = { Text("e.g. Physics Formula Sheet 2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownExpanded,
                    onExpandedChange = { isSubjectDropdownExpanded = !isSubjectDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = resourceSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isSubjectDropdownExpanded,
                        onDismissRequest = { isSubjectDropdownExpanded = false }
                    ) {
                        ADD_SUBJECT_OPTIONS.forEach { subj ->
                            DropdownMenuItem(
                                text = { Text(subj) },
                                onClick = {
                                    resourceSubject = subj
                                    isSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = resourceUrl,
                    onValueChange = { resourceUrl = it },
                    label = { Text("URL Link (Google Drive, Web)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (resourceTitle.isNotBlank()) {
                        onConfirm(resourceTitle.trim(), resourceSubject, resourceUrl.trim())
                    }
                },
                enabled = resourceTitle.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getSubjectColor(subject: String): Color {
    return when (subject.lowercase().trim()) {
        "all" -> Color(0xFF3B82F6) // Blue
        "general" -> Color(0xFFEAB308) // Warm Gold
        "physics" -> Color(0xFF06B6D4) // Cyan
        "chemistry" -> Color(0xFFF97316) // Orange
        "maths" -> Color(0xFF6366F1) // Indigo
        "biology" -> Color(0xFF10B981) // Emerald Green
        "english" -> Color(0xFF8B5CF6) // Purple / Violet
        "history" -> Color(0xFFD97706) // Amber / Terracotta
        "geography" -> Color(0xFF14B8A6) // Teal
        "computer applications" -> Color(0xFF0284C7) // Sky Blue
        "l2" -> Color(0xFFEC4899) // Pink / Rose
        "pyq" -> Color(0xFFF43F5E) // Rose Red
        else -> Color(0xFF3B82F6)
    }
}
