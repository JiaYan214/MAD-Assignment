@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.foodmanager.purchasing.ui


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodmanager.purchasing.data.PurchasingRepositoryFirebase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(repo: PurchasingRepositoryFirebase) {
    val orders by repo.ordersFlow().collectAsState(initial = emptyList())
    val fmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }


    Scaffold(topBar = { TopAppBar(title = { Text("Stock Purchasing History") }) }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { (order, lines) ->
                ElevatedCard {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        val time = Instant.ofEpochSecond(order.createdAt).atZone(ZoneId.systemDefault())
                        Text("Order: ${order.id.take(8)} • ${fmt.format(time)}")
                        Text("Total: RM ${"%.2f".format(order.totalCost)}")
                        Spacer(Modifier.height(4.dp))
                        lines.forEach { line -> Text("• ${line.qty} × ${"%.2f".format(line.unitPrice)} (inv=${line.inventoryId.take(6)})") }
                    }
                }
            }
        }
    }
}