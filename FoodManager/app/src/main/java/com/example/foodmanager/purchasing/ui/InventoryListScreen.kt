package com.example.foodmanager.purchasing.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.foodmanager.purchasing.data.Category
import com.example.foodmanager.purchasing.data.InventoryItem
import com.example.foodmanager.purchasing.vm.InventoryViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun InventoryListScreen(
    vm: InventoryViewModel,
    onGoPurchase: () -> Unit,
    onGoHistory: () -> Unit
) {
    val state by vm.state.collectAsState()

    var editMode by rememberSaveable { mutableStateOf(false) }

    var searchField by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(state.query))
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(searchField.text) {
        snapshotFlow { searchField.text }
            .debounce(200)
            .distinctUntilChanged()
            .collect { vm.onSearchChange(it) }
    }

    var showAdd by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.query) {
        if (state.query != searchField.text) {
            searchField = searchField.copy(text = state.query)
        }
    }

    LaunchedEffect(Unit) { vm.seedSample() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory List", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFE08A)),
                actions = {
                    IconButton(onClick = { editMode = !editMode }) {
                        Icon(Icons.Default.Edit, contentDescription = "Toggle edit")
                    }
                    IconButton(onClick = onGoPurchase) {
                        Icon(Icons.Default.Add, contentDescription = "Purchase")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFFFF3C4)) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onGoHistory) { Text("History") }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                text = { Text("Add") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(16.dp)
        ) {
            // Search (TextFieldValue to preserve cursor/selection)
            OutlinedTextField(
                value = searchField,
                onValueChange = { searchField = it },
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(TextStyle(color = Color.Black)),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Search for item") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Category chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip("All",        state.categoryFilter == Category.ALL)        { vm.onCategoryChange(Category.ALL) }
                CategoryChip("Ingredient", state.categoryFilter == Category.INGREDIENT){ vm.onCategoryChange(Category.INGREDIENT) }
                CategoryChip("Equipments", state.categoryFilter == Category.EQUIPMENT) { vm.onCategoryChange(Category.EQUIPMENT) }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items, key = { it.id }) { item ->
                    InventoryTile(
                        item = item,
                        editMode = editMode,
                        onDelete = { vm.deleteItem(item) },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddInventoryDialog(
            onDismiss = { showAdd = false },
            onConfirm = { newItem ->
                vm.saveItem(newItem)
                showAdd = false
            }
        )
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (selected) Color.Transparent else Color(0xFFE0E0E0)),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFFFF3C4),
            selectedContainerColor = Color(0xFFFFE08A),
            labelColor = Color(0xFF5B5B5B),
            selectedLabelColor = Color(0xFF3D3D3D),
        )
    )
}

@Composable
private fun StatusTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(text, color = Color.Black) }
}

@Composable
private fun InventoryTile(
    item: InventoryItem,
    editMode: Boolean,
    onDelete: () -> Unit,
) {
    val (label, tint) = when {
        item.stockQty <= 0.0 -> "unavailable" to Color(0xFFFFC1C1)
        item.stockQty <= item.reorderLevel -> "low stock" to Color(0xFFFFE08A)
        else -> "available" to Color(0xFFC5F3C2)
    }

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PIC placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEDEDED)),
                contentAlignment = Alignment.Center
            ) { Text("PIC") }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text("${item.stockQty} ${item.unit}", color = Color(0xFF6B6B6B))
                if (!editMode) {
                    Spacer(Modifier.height(8.dp))
                    Row { StatusTag(label, tint) }
                }
            }

            if (editMode) {
                // Only show a red delete button in edit mode
                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color(0xFFFFD6D6),
                        contentColor = Color(0xFF8A1212)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            } else {
                // No buttons in normal mode (mock shows edit/add floating buttons globally)
                Spacer(Modifier.width(0.dp))
            }
        }
    }
}

@Composable
private fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (InventoryItem) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.INGREDIENT) }
    var stock by rememberSaveable { mutableStateOf("0") }
    var reorder by rememberSaveable { mutableStateOf("5") }
    var price by rememberSaveable { mutableStateOf("0.0") }
    val canSave = name.isNotBlank() && unit.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onConfirm(
                        InventoryItem(
                            // id stays "", repo will set the doc id
                            name = name.trim(),
                            unit = unit.trim(),
                            category = category,
                            stockQty = stock.toDoubleOrNull() ?: 0.0,
                            reorderLevel = reorder.toDoubleOrNull() ?: 0.0,
                            pricePerUnit = price.toDoubleOrNull() ?: 0.0,
                            isAvailable = true
                        )
                    )
                }
            ) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Inventory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true
                )
                OutlinedTextField(
                    value = unit, onValueChange = { unit = it },
                    label = { Text("Unit (kg, bottle, pkg…)") }, singleLine = true
                )

                // category selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { category = Category.INGREDIENT },
                        label = { Text("Ingredient") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (category == Category.INGREDIENT) Color(0xFFFFE08A) else Color(0xFFF1F1F1)
                        )
                    )
                    AssistChip(
                        onClick = { category = Category.EQUIPMENT },
                        label = { Text("Equipment") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (category == Category.EQUIPMENT) Color(0xFFFFE08A) else Color(0xFFF1F1F1)
                        )
                    )
                }

                OutlinedTextField(
                    value = stock, onValueChange = { stock = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Stock qty") }, singleLine = true
                )
                OutlinedTextField(
                    value = reorder, onValueChange = { reorder = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Reorder level") }, singleLine = true
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Price per unit (RM)") }, singleLine = true
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}


