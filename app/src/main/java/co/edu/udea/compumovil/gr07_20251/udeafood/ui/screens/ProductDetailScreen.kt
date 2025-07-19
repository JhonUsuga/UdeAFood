package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(productId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var product by remember { mutableStateOf<Product?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    val userId = auth.currentUser?.uid.orEmpty()
    var existingReviewId by remember { mutableStateOf<String?>(null) }

    suspend fun loadReviews(storeId: String) {
        val snapshot = db.collection("stores")
            .document(storeId)
            .collection("products")
            .document(productId)
            .collection("reviews")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()
        reviews = snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
    }

    suspend fun loadUserReview(storeId: String) {
        val snapshot = db.collection("stores")
            .document(storeId)
            .collection("products")
            .document(productId)
            .collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        if (!snapshot.isEmpty) {
            val doc = snapshot.documents.first()
            existingReviewId = doc.id
            val review = doc.toObject(Review::class.java)
            rating = review?.rating?.toInt() ?: 0
            comment = review?.comment.orEmpty()
        } else {
            existingReviewId = null
            rating = 0
            comment = ""
        }
    }

    LaunchedEffect(productId) {
        val doc = db.collectionGroup("products")
            .whereEqualTo("id", productId)
            .get()
            .await()

        if (!doc.isEmpty) {
            product = doc.documents.first().toObject(Product::class.java)
            product?.storeId?.let {
                loadReviews(it)
                loadUserReview(it)
            }
        }
    }

    product?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle del producto") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Star, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(it.imageUrl),
                    contentDescription = it.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )

                Text(it.name, style = MaterialTheme.typography.headlineSmall)
                Text("Precio: $${it.price}", style = MaterialTheme.typography.bodyLarge)
                Text(it.description, style = MaterialTheme.typography.bodyMedium)

                Divider()

                Text("Califica este producto:")
                Row {
                    (1..5).forEach { i ->
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i estrellas",
                                tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val review = Review(
                            userId = userId,
                            userName = auth.currentUser?.displayName ?: "Usuario",
                            rating = rating.toFloat(),
                            comment = comment
                        )

                        val reviewRef = db.collection("stores")
                            .document(it.storeId)
                            .collection("products")
                            .document(productId)
                            .collection("reviews")

                        if (existingReviewId != null) {
                            reviewRef.document(existingReviewId!!).set(review)
                        } else {
                            reviewRef.add(review)
                        }

                        scope.launch {
                            loadReviews(it.storeId)
                            loadUserReview(it.storeId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (existingReviewId != null) "Actualizar reseña" else "Enviar calificación")
                }

                // Nuevo: botón para eliminar reseña
                if (existingReviewId != null) {
                    OutlinedButton(
                        onClick = {
                            db.collection("stores")
                                .document(it.storeId)
                                .collection("products")
                                .document(productId)
                                .collection("reviews")
                                .document(existingReviewId!!)
                                .delete()
                                .addOnSuccessListener {
                                    scope.launch {
                                        product?.storeId?.let { storeId ->
                                            loadReviews(storeId)
                                            loadUserReview(storeId)
                                        }
                                    }
                                }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar mi reseña")
                    }
                }

                Button(
                    onClick = {
                        product?.storeId?.let { storeId ->
                            navController.navigate("store_products/$storeId")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Ir a la tienda")
                }

                val isFavorite = remember { mutableStateOf(product?.favoritedBy?.contains(userId) == true) }
                val snackbarHostState = remember { SnackbarHostState() }

                Button(
                    onClick = {
                        val productRef = db.collection("stores")
                            .document(it.storeId)
                            .collection("products")
                            .document(productId)

                        val favoritesRef = db.collection("users")
                            .document(userId)
                            .collection("favorites")
                            .document(productId)

                        val isCurrentlyFavorite = isFavorite.value

                        if (isCurrentlyFavorite) {
                            // Quitar de favoritos
                            productRef.update("favoritedBy", it.favoritedBy.filter { uid -> uid != userId })
                            favoritesRef.delete()
                                .addOnSuccessListener {
                                    isFavorite.value = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Producto eliminado de favoritos")
                                    }
                                }
                        } else {
                            // Agregar a favoritos
                            productRef.update("favoritedBy", it.favoritedBy.plus(userId))
                            val favoriteData = hashMapOf(
                                "productId" to it.id,
                                "storeId" to it.storeId
                            )
                            favoritesRef.set(favoriteData)
                                .addOnSuccessListener {
                                    isFavorite.value = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Producto agregado a favoritos")
                                    }
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isFavorite.value) "Quitar de favoritos" else "Agregar a favoritos")
                }

                if (reviews.isNotEmpty()) {
                    Divider()
                    Text("Últimas reseñas", style = MaterialTheme.typography.titleMedium)

                    reviews.forEach { review ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = review.userName, style = MaterialTheme.typography.labelLarge)
                                Text(text = "⭐".repeat(review.rating.toInt()))
                                Text(text = review.comment)
                            }
                        }
                    }
                }
            }
        }
    }
}