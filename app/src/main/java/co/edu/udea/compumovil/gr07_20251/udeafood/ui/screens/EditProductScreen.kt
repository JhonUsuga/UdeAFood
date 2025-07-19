package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProductScreen(storeId: String, productId: String, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoaded by remember { mutableStateOf(false) }

    // Cargar datos del producto una sola vez
    LaunchedEffect(Unit) {
        db.collection("stores")
            .document(storeId)
            .collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    name = product.name
                    description = product.description
                    price = product.price.toString()
                    imageUrl = product.imageUrl
                    isLoaded = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar producto", Toast.LENGTH_SHORT).show()
            }
    }

    if (isLoaded) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editar Producto", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("URL de Imagen") })

            Button(onClick = {
                val updatedProduct = hashMapOf(
                    "name" to name,
                    "description" to description,
                    "price" to price.toDoubleOrNull(),
                    "imageUrl" to imageUrl
                )

                db.collection("stores")
                    .document(storeId)
                    .collection("products")
                    .document(productId)
                    .update(updatedProduct as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            }) {
                Text("Guardar cambios")
            }
        }
    }
}