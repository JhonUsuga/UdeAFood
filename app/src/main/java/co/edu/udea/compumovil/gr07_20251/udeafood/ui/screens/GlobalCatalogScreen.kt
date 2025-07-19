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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCatalogScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Nombre (A-Z)") }

    val sortOptions = listOf("Nombre (A-Z)", "Precio ↑", "Rating ↓")

    // Cargar productos
    LaunchedEffect(Unit) {
        val allProducts = mutableListOf<Product>()
        try {
            val storesSnapshot = db.collection("stores").get().await()
            for (storeDoc in storesSnapshot.documents) {
                val storeId = storeDoc.id
                val productSnapshot = db.collection("stores")
                    .document(storeId)
                    .collection("products")
                    .get()
                    .await()

                val storeProducts = productSnapshot.documents.mapNotNull {
                    it.toObject(Product::class.java)
                }

                allProducts.addAll(storeProducts)
            }

            products = allProducts
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar producto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = { minPrice = it },
                label = { Text("Precio min") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it },
                label = { Text("Precio max") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de orden
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = sortOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("Ordenar por") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        sortOption = option
                        expanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            val filtered by remember(searchQuery, minPrice, maxPrice, sortOption, products) {
                mutableStateOf(
                    products.filter {
                        it.name.contains(searchQuery, ignoreCase = true) &&
                                (minPrice.toDoubleOrNull()?.let { min -> it.price >= min } ?: true) &&
                                (maxPrice.toDoubleOrNull()?.let { max -> it.price <= max } ?: true)
                    }.let {
                        when (sortOption) {
                            "Precio ↑" -> it.sortedBy { p -> p.price }
                            "Rating ↓" -> it.sortedByDescending { p -> p.rating }
                            else -> it.sortedBy { p -> p.name.lowercase() }
                        }
                    }
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { product ->
                    ProductCard(product = product, navController = navController)
                }
            }
        }
    }
}