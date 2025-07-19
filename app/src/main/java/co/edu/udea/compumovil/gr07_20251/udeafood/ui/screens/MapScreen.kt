package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import co.edu.udea.compumovil.gr07_20251.udeafood.R
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Store
import co.edu.udea.compumovil.gr07_20251.udeafood.util.getKnownProductIds
import co.edu.udea.compumovil.gr07_20251.udeafood.util.isStoreOpen
import co.edu.udea.compumovil.gr07_20251.udeafood.util.saveKnownProductIds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    onAccountClick: (String) -> Unit,
    navController: NavHostController
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var userType by remember { mutableStateOf<String?>(null) }

    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            firestore.collection("stores").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) userType = "store"
                    else {
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) userType = "client"
                            }
                    }
                }
        }

        if (userType == "client" && currentUser != null) {
            try {
                val favoritesSnapshot = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("favorites")
                    .get()
                    .await()

                val favoriteStoreIds = favoritesSnapshot.documents.mapNotNull { it.getString("storeId") }

                val currentProductIds = mutableSetOf<String>()
                for (storeId in favoriteStoreIds) {
                    val productsSnapshot = firestore.collection("stores")
                        .document(storeId)
                        .collection("products")
                        .get()
                        .await()

                    productsSnapshot.documents.forEach { doc ->
                        doc.getString("id")?.let { currentProductIds.add(it) }
                    }
                }

                val knownIds = getKnownProductIds(context)
                val newOnes = currentProductIds.subtract(knownIds)

                if (newOnes.isNotEmpty()) {
                    saveKnownProductIds(context, currentProductIds)
                    scope.launch {
                        snackbarHostState.showSnackbar("¡Nuevos productos en tus tiendas favoritas!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Botón para ver el catálogo global
                FloatingActionButton(
                    onClick = { navController.navigate("global_catalog") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("🛍️")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón de cuenta con menú
                Box {
                    ExtendedFloatingActionButton(
                        onClick = { expanded = true },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Cuenta") },
                        text = { Text("Cuenta") }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi cuenta") },
                            onClick = {
                                expanded = false
                                userType?.let { onAccountClick(it) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                expanded = false
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("unified_login") {
                                    popUpTo("map") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        AndroidView(factory = {
            val mapView = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(18.9)
                controller.setCenter(GeoPoint(6.2663, -75.5688))
            }

            firestore.collection("stores").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val store = document.toObject(Store::class.java)
                        val lat = document.getDouble("lat") ?: continue
                        val lng = document.getDouble("lng") ?: continue

                        val marker = Marker(mapView).apply {
                            position = GeoPoint(lat, lng)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = store.name
                            icon = ContextCompat.getDrawable(
                                context,
                                if (isStoreOpen(store.openHour, store.closeHour))
                                    R.drawable.ic_store_open
                                else
                                    R.drawable.ic_store_closed
                            )

                            // Navegar al catálogo cuando se toca el marcador
                            setOnMarkerClickListener { _, _ ->
                                navController.navigate("store_catalog/${store.id}")
                                true
                            }
                        }

                        mapView.overlays.add(marker)
                    }
                    mapView.invalidate()
                }

            mapView
        }, modifier = Modifier.fillMaxSize())
    }
}