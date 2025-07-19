package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Review
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreReviewManagerScreen() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val storeId = currentUser?.uid ?: return

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var productReviews by remember { mutableStateOf<Map<String, List<Review>>>(emptyMap()) }

    // Cargar productos y reseñas
    LaunchedEffect(storeId) {
        val productSnapshot = db.collection("stores")
            .document(storeId)
            .collection("products")
            .get()
            .await()

        val fetchedProducts = productSnapshot.documents.mapNotNull { it.toObject(Product::class.java) }
        products = fetchedProducts

        val reviewsMap = mutableMapOf<String, List<Review>>()
        for (product in fetchedProducts) {
            val reviewsSnapshot = db.collection("stores")
                .document(storeId)
                .collection("products")
                .document(product.id)
                .collection("reviews")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.mapNotNull { it.toObject(Review::class.java) }
            reviewsMap[product.id] = reviews
        }

        productReviews = reviewsMap
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Reseñas de tus productos", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text("⭐ ${product.rating} - ${product.reviewCount} reseñas")

                        val reviews = productReviews[product.id] ?: emptyList()
                        if (reviews.isEmpty()) {
                            Text("Sin reseñas aún")
                        } else {
                            reviews.forEach { review ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Divider()
                                Text(review.userName, style = MaterialTheme.typography.labelLarge)
                                Text("⭐".repeat(review.rating.toInt()))
                                Text(review.comment)
                            }
                        }
                    }
                }
            }
        }
    }
}