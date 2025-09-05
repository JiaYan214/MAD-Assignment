package com.example.foodmanager.purchasing.data


import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class PurchasingRepositoryFirebase(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val storage: FirebaseStorage? = Firebase.storage
) {
    private val inventoryCol = db.collection(FirestorePaths.INVENTORY)
    private val ordersCol = db.collection(FirestorePaths.ORDERS)


    fun inventoryFlow(query: String?): Flow<List<InventoryItem>> = callbackFlow {
        val reg = inventoryCol.orderBy("name")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val items = (snap?.documents ?: emptyList()).map { doc ->
// Firestore can map enums if stored as strings matching names
                    val data = doc.toObject(InventoryItem::class.java) ?: InventoryItem()
                    data.copy(id = doc.id)
                }.let { list ->
                    if (query.isNullOrBlank()) list else list.filter { it.name.contains(query, true) }
                }
                trySend(items)
            }
        awaitClose { reg.remove() }
    }


    suspend fun upsertItem(item: InventoryItem): String = suspendCancellableCoroutine { cont ->
        val doc = if (item.id.isBlank()) inventoryCol.document() else inventoryCol.document(item.id)
        doc.set(item.copy(id = doc.id))
            .addOnSuccessListener { cont.resume(doc.id) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun seedIfEmpty(seed: List<InventoryItem>) {
        val existing = inventoryCol.limit(1).get().await()
        if (existing.isEmpty) {
            db.runBatch { b ->
                seed.forEach { s ->
                    val d = inventoryCol.document()
                    b.set(d, s.copy(id = d.id))
                }
            }.await()
        }
    }

    /** Create order + lines + increment inventory using a Firestore transaction */
    suspend fun confirmPurchase(cart: List<Pair<InventoryItem, Double>>) {
        require(cart.isNotEmpty())
        val total = cart.sumOf { (it.first.pricePerUnit * it.second) }
        db.runTransaction { tx ->
            val orderRef = ordersCol.document()
            tx.set(orderRef, PurchaseOrder(id = orderRef.id, totalCost = total))


            cart.forEach { (item, qty) ->
                val lineRef = orderRef.collection(FirestorePaths.LINES).document()
                tx.set(lineRef, PurchaseLine(id = lineRef.id, inventoryId = item.id, qty = qty, unitPrice = item.pricePerUnit))


                val invRef = inventoryCol.document(item.id)
                val snap = tx.get(invRef)
                val current = snap.getDouble("stockQty") ?: 0.0
                tx.update(invRef, mapOf(
                    "stockQty" to (current + qty),
                    "isAvailable" to true
                ))
            }
        }.await()
    }

    fun ordersFlow(): Flow<List<Pair<PurchaseOrder, List<PurchaseLine>>>> = callbackFlow {
        val reg = ordersCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val orderDocs = snap?.documents.orEmpty()
                if (orderDocs.isEmpty()) { trySend(emptyList()); return@addSnapshotListener }


                val tasks = orderDocs.map { oDoc ->
                    oDoc.reference.collection(FirestorePaths.LINES).get().continueWith { t ->
                        val order = oDoc.toObject(PurchaseOrder::class.java) ?: PurchaseOrder()
                        val lines = t.result?.documents.orEmpty().map { l ->
                            val line = l.toObject(PurchaseLine::class.java) ?: PurchaseLine()
                            line.copy(id = l.id)
                        }
                        order.copy(id = oDoc.id) to lines
                    }
                }
                Tasks.whenAllSuccess<Pair<PurchaseOrder, List<PurchaseLine>>>(tasks)
                    .addOnSuccessListener { trySend(it) }
                    .addOnFailureListener { close(it) }
            }
        awaitClose { reg.remove() }
    }


    suspend fun uploadImage(itemId: String, bytes: ByteArray): String {
        val st = storage ?: error("Storage not configured")
        val ref = st.reference.child("inventory/$itemId.jpg")
        ref.putBytes(bytes).await()
        val url = ref.downloadUrl.await().toString()
        inventoryCol.document(itemId).update("imageUri", url).await()
        return url
    }

    suspend fun deleteItem(id: String) {
        inventoryCol.document(id).delete().await()
    }
}