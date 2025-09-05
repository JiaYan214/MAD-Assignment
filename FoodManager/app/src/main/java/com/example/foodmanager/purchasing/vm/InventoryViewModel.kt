package com.example.foodmanager.purchasing.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.purchasing.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventoryUiState(
    val query: String = "",
    val items: List<InventoryItem> = emptyList(),
    val categoryFilter: Category = Category.ALL
)

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(private val repo: PurchasingRepositoryFirebase) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow(Category.ALL)

    val state: StateFlow<InventoryUiState> =
        combine(query, category) { q, cat -> q to cat }
            .flatMapLatest { (q, cat) ->
                repo.inventoryFlow(q).map { items ->
                    val filtered = if (cat == Category.ALL) items else items.filter { it.category == cat }
                    InventoryUiState(q, filtered, cat)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InventoryUiState()
            )

    fun onSearchChange(v: String) { query.value = v }
    fun onCategoryChange(c: Category) { category.value = c }

    fun saveItem(item: InventoryItem) = viewModelScope.launch { repo.upsertItem(item) }
    fun deleteItem(item: InventoryItem) = viewModelScope.launch {
        if (item.id.isNotBlank()) repo.deleteItem(item.id)
    }

    fun seedSample() = viewModelScope.launch {
        repo.seedIfEmpty(
            listOf(
                InventoryItem(name = "RICE", unit = "package", category = Category.INGREDIENT, stockQty = 30.0, reorderLevel = 10.0, pricePerUnit = 8.5),
                InventoryItem(name = "Tomato", unit = "kg", category = Category.INGREDIENT, stockQty = 5.0, reorderLevel = 8.0, pricePerUnit = 6.0),
                InventoryItem(name = "Cooking Oil", unit = "bottle", category = Category.INGREDIENT, stockQty = 2.0, reorderLevel = 3.0, pricePerUnit = 12.0),
                InventoryItem(name = "Chicken Breast", unit = "kg", category = Category.INGREDIENT, stockQty = 0.0, reorderLevel = 5.0, pricePerUnit = 14.0),
                InventoryItem(name = "Knife", unit = "unit", category = Category.EQUIPMENT, stockQty = 4.0, reorderLevel = 2.0, pricePerUnit = 25.0),
                InventoryItem(name = "Blender", unit = "unit", category = Category.EQUIPMENT, stockQty = 1.0, reorderLevel = 1.0, pricePerUnit = 120.0)
            )
        )
    }
}
