package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Product
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun AddProductScreen(storeId: String, navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Agregar producto", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Precio") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = GreenActive)
        ) {
            Text("Seleccionar imagen", color = MaterialTheme.colorScheme.onPrimary)
        }

        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Vista previa de imagen",
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                val currentStoreId = auth.currentUser?.uid
                if (currentStoreId != null && name.isNotBlank() && price.toDoubleOrNull() != null && imageUri != null) {
                    val productId = UUID.randomUUID().toString()
                    val product = Product(
                        id = productId,
                        name = name,
                        description = description,
                        price = price.toDouble(),
                        imageUrl = imageUri.toString(), // Temporalmente como URI local
                        storeId = currentStoreId
                    )

                    db.collection("stores")
                        .document(currentStoreId)
                        .collection("products")
                        .document(productId)
                        .set(product)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "Completa todos los campos y selecciona una imagen", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenActive)
        ) {
            Text("Guardar producto", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}