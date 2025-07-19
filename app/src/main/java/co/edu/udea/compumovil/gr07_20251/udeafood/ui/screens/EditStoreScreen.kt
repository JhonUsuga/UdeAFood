package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStoreScreen(storeId: String, navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationDesc by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf("09:00") }
    var closeHour by remember { mutableStateOf("21:00") }
    val daysOpen = remember {
        mutableStateMapOf(
            "Lunes" to false, "Martes" to false, "Miércoles" to false,
            "Jueves" to false, "Viernes" to false, "Sábado" to false, "Domingo" to false
        )
    }

    val scrollState = rememberScrollState()

    // Cargar datos desde Firestore
    LaunchedEffect(storeId) {
        db.collection("stores").document(storeId).get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                description = document.getString("description") ?: ""
                locationDesc = document.getString("locationDesc") ?: ""
                openHour = document.getString("openHour") ?: ""
                closeHour = document.getString("closeHour") ?: ""
                val openDaysMap = document.get("daysOpen") as? Map<*, *>
                openDaysMap?.forEach { (key, _) ->
                    daysOpen[key.toString()] = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar tienda", Toast.LENGTH_SHORT).show()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Editar Tienda", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
        OutlinedTextField(value = locationDesc, onValueChange = { locationDesc = it }, label = { Text("Ubicación detallada") })

        TimePickerField("Hora de apertura", openHour) { openHour = it }
        TimePickerField("Hora de cierre", closeHour) { closeHour = it }

        Text("Días de apertura", style = MaterialTheme.typography.titleMedium)
        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(day)
                Switch(
                    checked = daysOpen[day] ?: false,
                    onCheckedChange = { daysOpen[day] = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = GreenActive)
                )
            }
        }

        Button(
            onClick = {
                val updatedStore = mapOf(
                    "name" to name,
                    "description" to description,
                    "locationDesc" to locationDesc,
                    "openHour" to openHour,
                    "closeHour" to closeHour,
                    "daysOpen" to daysOpen.filterValues { it }
                )
                db.collection("stores").document(storeId).update(updatedStore)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Tienda actualizada", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // O ir de nuevo al dashboard
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }
    }
}
