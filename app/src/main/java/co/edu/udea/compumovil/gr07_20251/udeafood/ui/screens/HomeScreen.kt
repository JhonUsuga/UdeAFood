package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Store
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.components.StoreCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val featured = remember { mockStores().firstOrNull { it.isFeatured } }
    val allStores = remember { mockStores() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("UdeaFood") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            featured?.let {
                Text("Featured", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                StoreCard(store = it, onClick = { /* Navegar */ })
            }

            Text("All restaurants", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            allStores.forEach {
                StoreCard(store = it, onClick = { /* Navegar */ })
            }
        }
    }
}

fun mockStores(): List<Store> = listOf(
    Store(
        id = "1",
        name = "Ala Delta",
        category = "Vegetarian · Healthy",
        imageUrl = "https://i.imgur.com/GzYqZ4v.jpg",
        isFeatured = true,
        rating = 4.5f,
        reviewCount = 122,
        closeHour = "10:00 PM"
    ),
    Store(
        id = "2",
        name = "Malena Baristas",
        category = "Cafe",
        imageUrl = "https://i.imgur.com/KOE4pC0.jpg",
        rating = 4.3f,
        reviewCount = 235
    ),
    Store(
        id = "3",
        name = "Sabor Isabel",
        category = "Burgers · Salads",
        imageUrl = "https://i.imgur.com/MsF9QBG.jpg",
        rating = 4.0f,
        reviewCount = 128
    )
)