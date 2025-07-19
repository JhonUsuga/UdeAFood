package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import co.edu.udea.compumovil.gr07_20251.udeafood.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddReviewScreen(productId: String, storeId: String, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var rating by remember { mutableStateOf(3f) }
    var comment by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dejar una reseña", style = MaterialTheme.typography.headlineSmall)

        Text("Calificación:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (i in 1..5) {
                IconButton(onClick = { rating = i.toFloat() }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
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
                if (currentUser != null && comment.text.isNotBlank()) {
                    val review = Review(
                        userId = currentUser.uid,
                        userName = currentUser.displayName ?: "Usuario",
                        rating = rating,
                        comment = comment.text
                    )

                    val productRef = db.collection("stores").document(storeId).collection("products").document(productId)

                    // Guardar reseña
                    db.collection("stores")
                        .document(storeId)
                        .collection("products")
                        .document(productId)
                        .collection("reviews")
                        .add(review)
                        .addOnSuccessListener {
                            updateProductRating(productRef)
                            Toast.makeText(context, "¡Reseña guardada!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar reseña", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Enviar reseña", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
private fun updateProductRating(productRef: com.google.firebase.firestore.DocumentReference) {
    productRef.collection("reviews").get().addOnSuccessListener { snapshot ->
        val reviews = snapshot.documents.mapNotNull { it.toObject(Review::class.java) }
        if (reviews.isNotEmpty()) {
            val avg = reviews.map { it.rating }.average().toFloat()
            val count = reviews.size
            val latest = reviews.maxByOrNull { it.timestamp }

            val updateData = mapOf(
                "rating" to avg,
                "reviewCount" to count,
                "latestComment" to latest?.comment.orEmpty(),
                "latestReviewer" to latest?.userName.orEmpty()
            )

            productRef.update(updateData)
        }
    }
}
