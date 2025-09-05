package com.example.foodmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.foodmanager.purchasing.data.PurchasingRepositoryFirebase
import com.example.foodmanager.purchasing.ui.*
import com.example.foodmanager.purchasing.vm.InventoryViewModel
import com.example.foodmanager.purchasing.vm.PurchaseViewModel
import com.example.foodmanager.ui.theme.FoodManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = PurchasingRepositoryFirebase(db = Firebase.firestore, storage = null)
        val inventoryVM = InventoryViewModel(repo)
        val purchaseVM  = PurchaseViewModel(repo)

        setContent {
            FoodManagerTheme {
                val nav = rememberNavController()
                NavHost(navController = nav, startDestination = ROUTE_PURCHASING) {
                    purchasingGraph(
                        inventoryScreen = {
                            InventoryListScreen(
                                vm = inventoryVM,
                                onGoPurchase = { nav.navigate(ROUTE_PURCHASE) },
                                onGoHistory  = { nav.navigate(ROUTE_HISTORY) }
                            )
                        },
                        purchaseScreen = {
                            PurchaseScreen(
                                vm = purchaseVM,
                                onOpenCart = { nav.navigate(ROUTE_CART) }
                            )
                        },
                        cartScreen = {
                            CartScreen(
                                vm = purchaseVM,
                                onConfirmed = { nav.popBackStack(ROUTE_INVENTORY, false) }
                            )
                        },
                        historyScreen = {
                            HistoryScreen(repo = repo)
                        }
                    )
                }
            }
        }
    }
}