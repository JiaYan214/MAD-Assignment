@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.foodmanager.purchasing.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodmanager.purchasing.vm.PurchaseViewModel

@Composable
fun CartScreen(vm: PurchaseViewModel, onConfirmed: () -> Unit) {
    val st by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFE08A))
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color(0xFFFFF3C4)) {
                Spacer(Modifier.weight(1f))
                Button(
                    shape = RoundedCornerShape(18.dp),
                    onClick = { vm.confirm(onConfirmed) },
                    modifier = Modifier.padding(end = 16.dp)
                ) { Text("Confirm") }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Cart", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(st.cart, key = { it.item.id }) { line ->
                            ElevatedCard(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF8F8F8))
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // PIC
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFEDEDED)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("PIC") }

                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(line.item.name, fontWeight = FontWeight.SemiBold)
                                        Text("Amount : ${line.qty.toInt()} ${line.item.unit}")
                                    }
                                    Text(
                                        "RM ${"%.2f".format(line.item.pricePerUnit * line.qty)}",
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Total: RM ${"%.2f".format(st.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
