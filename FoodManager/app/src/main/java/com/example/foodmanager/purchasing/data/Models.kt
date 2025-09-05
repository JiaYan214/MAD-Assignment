package com.example.foodmanager.purchasing.data


enum class Category { ALL, INGREDIENT, EQUIPMENT }


data class InventoryItem(
    val id: String = "", // Firestore document ID
    val name: String = "",
    val unit: String = "",
    val category: Category = Category.INGREDIENT,
    val stockQty: Double = 0.0,
    val reorderLevel: Double = 5.0,
    val pricePerUnit: Double = 0.0,
    val imageUri: String? = null,
    val isAvailable: Boolean = true
)


data class PurchaseOrder(
    val id: String = "",
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val totalCost: Double = 0.0
)


data class PurchaseLine(
    val id: String = "",
    val inventoryId: String = "",
    val qty: Double = 0.0,
    val unitPrice: Double = 0.0
)


object FirestorePaths {
    const val INVENTORY = "inventory"
    const val ORDERS = "purchaseOrders"
    const val LINES = "lines"
}
