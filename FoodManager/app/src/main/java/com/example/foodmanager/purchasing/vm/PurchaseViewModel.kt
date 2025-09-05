package com.example.foodmanager.purchasing.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.purchasing.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


data class CartLine(val item: InventoryItem, val qty: Double)


data class PurchaseUiState(
    val catalog: List<InventoryItem> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val total: Double = 0.0
)


class PurchaseViewModel(private val repo: PurchasingRepositoryFirebase) : ViewModel() {
    private val cart = MutableStateFlow<List<CartLine>>(emptyList())


    val state: StateFlow<PurchaseUiState> =
        repo.inventoryFlow(null)
            .combine(cart) { items, lines ->
                val total = lines.sumOf { it.item.pricePerUnit * it.qty }
                PurchaseUiState(items, lines, total)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PurchaseUiState())


    fun inc(item: InventoryItem) { change(item, +1.0) }
    fun dec(item: InventoryItem) { change(item, -1.0) }


    private fun change(item: InventoryItem, delta: Double) {
        val list = cart.value.toMutableList()
        val idx = list.indexOfFirst { it.item.id == item.id }
        if (idx >= 0) {
            val newQty = (list[idx].qty + delta).coerceAtLeast(0.0)
            if (newQty == 0.0) list.removeAt(idx) else list[idx] = list[idx].copy(qty = newQty)
        } else if (delta > 0) list.add(CartLine(item, delta))
        cart.value = list
    }


    fun confirm(onDone: (() -> Unit)? = null) = viewModelScope.launch {
        val pairs = cart.value.map { it.item to it.qty }
        if (pairs.isNotEmpty()) {
            repo.confirmPurchase(pairs)
            cart.value = emptyList()
            onDone?.invoke()
        }
    }
}