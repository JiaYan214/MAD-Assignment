@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.foodmanager.purchasing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodmanager.purchasing.data.InventoryItem
import com.example.foodmanager.purchasing.vm.PurchaseViewModel

@Composable
fun PurchaseScreen(vm: PurchaseViewModel, onOpenCart: () -> Unit) {
    val st by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFE08A)),
                actions = {
                    IconButton(onClick = onOpenCart) {
                        BadgedBox(badge = {
                            if (st.cart.isNotEmpty()) {
                                Badge { Text(st.cart.sumOf { it.qty }.toInt().toString()) }
                            }
                        }) { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") }
                    }
                }
            )
        },
        bottomBar = {
            // Bottom “Add to cart / totals” bar like your mock
            BottomAppBar(containerColor = Color(0xFFFFF3C4)) {
                Text(
                    "Total: RM ${"%.2f".format(st.total)}",
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(Modifier.weight(1f))
                Button(
                    shape = RoundedCornerShape(18.dp),
                    onClick = onOpenCart,
                    modifier = Modifier.padding(end = 16.dp)
                ) { Text("Cart (${st.cart.sumOf { it.qty }.toInt()})") }
            }
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(st.catalog, key = { it.id }) { item ->
                PurchaseRow(
                    item = item,
                    qtyInCart = st.cart.find { it.item.id == item.id }?.qty ?: 0.0,
                    onPlus = { vm.inc(item) },
                    onMinus = { vm.dec(item) }
                )
            }
        }
    }
}

@Composable
private fun PurchaseRow(
    item: InventoryItem,
    qtyInCart: Double,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEDEDED)),
                contentAlignment = Alignment.Center
            ) { Text("PIC") }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(2.dp))
                Text("RM ${"%.2f".format(item.pricePerUnit)} / ${item.unit}", color = Color(0xFF6B6B6B))
            }

            QtyStepper(
                value = qtyInCart,
                onDec = onMinus,
                onInc = onPlus
            )
        }
    }
}

@Composable
private fun QtyStepper(
    value: Double,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDec,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) { Text("–") }

        Surface(
            tonalElevation = 1.dp,
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFF6F6F6)
        ) {
            Text(
                "%d".format(value.toInt()),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        OutlinedButton(
            onClick = onInc,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) { Text("+") }
    }
}
