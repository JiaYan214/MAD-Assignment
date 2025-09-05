package com.example.foodmanager.purchasing.ui


import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation


const val ROUTE_PURCHASING = "purchasing"
const val ROUTE_INVENTORY = "inventory"
const val ROUTE_PURCHASE = "purchase"
const val ROUTE_CART = "cart"
const val ROUTE_HISTORY = "history"


fun NavGraphBuilder.purchasingGraph(
    inventoryScreen: @Composable () -> Unit,
    purchaseScreen: @Composable () -> Unit,
    cartScreen: @Composable () -> Unit,
    historyScreen: @Composable () -> Unit
) {
    navigation(startDestination = ROUTE_INVENTORY, route = ROUTE_PURCHASING) {
        composable(ROUTE_INVENTORY) { inventoryScreen() }
        composable(ROUTE_PURCHASE) { purchaseScreen() }
        composable(ROUTE_CART) { cartScreen() }
        composable(ROUTE_HISTORY) { historyScreen() }
    }
}