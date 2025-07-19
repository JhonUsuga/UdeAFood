package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.ProductCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val favSnapshot = db.collection("users").document(userId).collection("favorites").get().await()
            val favorites = favSnapshot.documents.mapNotNull {
                val productId = it.getString("productId") ?: return@mapNotNull null
                val storeId = it.getString("storeId") ?: return@mapNotNull null

                val productDoc = db.collection("stores").document(storeId)
                    .collection("products").document(productId).get().await()

                productDoc.toObject(Product::class.java)
            }
            products = favorites
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Favoritos") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (products.isEmpty()) {
                Text("No tienes productos favoritos.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(products) { product ->
                        Column {
                            ProductCard(product = product, navController = navController)

                            Button(
                                onClick = {
                                    val updatedList = product.favoritedBy.filter { it != userId }

                                    db.collection("stores")
                                        .document(product.storeId)
                                        .collection("products")
                                        .document(product.id)
                                        .update("favoritedBy", updatedList)
                                        .addOnSuccessListener {
                                            products = products.filter { it.id != product.id }
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Producto eliminado de favoritos")
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Quitar de favoritos", color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
