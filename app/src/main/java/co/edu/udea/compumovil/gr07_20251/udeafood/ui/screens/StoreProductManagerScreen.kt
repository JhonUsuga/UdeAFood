package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun StoreProductManagerScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storeId = auth.currentUser?.uid

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    LaunchedEffect(Unit) {
        storeId?.let {
            db.collection("stores").document(it).collection("products").get()
                .addOnSuccessListener { result ->
                    products = result.documents.mapNotNull { it.toObject(Product::class.java) }
                }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Mis productos", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("$${product.price}")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(product.description)

                        if (product.imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(product.imageUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                // Ir a pantalla de edición (próxima que haremos)
                                navController.navigate("edit_product/${product.id}")
                            }, colors = ButtonDefaults.buttonColors(containerColor = GreenActive)) {
                                Text("Editar")
                            }

                            Button(onClick = {
                                db.collection("stores")
                                    .document(storeId!!)
                                    .collection("products")
                                    .document(product.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                        products = products.filter { it.id != product.id }
                                    }
                            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
