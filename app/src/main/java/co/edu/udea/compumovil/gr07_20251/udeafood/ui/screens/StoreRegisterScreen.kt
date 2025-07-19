package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.app.TimePickerDialog
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenActive
import co.edu.udea.compumovil.gr07_20251.udeafood.ui.theme.GreenTrack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun StoreRegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationDesc by remember { mutableStateOf("") }
    var openHour by remember { mutableStateOf("09:00") }
    var closeHour by remember { mutableStateOf("21:00") }
    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val daysOpen = remember {
        mutableStateMapOf(
            "Lunes" to false, "Martes" to false, "Miércoles" to false,
            "Jueves" to false, "Viernes" to false, "Sábado" to false, "Domingo" to false
        )
    }

    val dayOrder = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val scrollState = rememberScrollState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Registrar tienda", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })

        Button(
            onClick = { navController.navigate("map_picker") },
            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elegir ubicación en mapa", color = Color.White)
        }

        OutlinedTextField(value = locationDesc, onValueChange = { locationDesc = it }, label = { Text("Descripción de ubicación") })

        TimePickerField("Hora de apertura", openHour) { selected -> openHour = selected }
        TimePickerField("Hora de cierre", closeHour) { selected -> closeHour = selected }

        Text("Días de apertura", style = MaterialTheme.typography.titleMedium)

        dayOrder.forEach { day ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(day)
                Switch(
                    checked = daysOpen[day] ?: false,
                    onCheckedChange = { isChecked -> daysOpen[day] = isChecked },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenActive,
                        checkedTrackColor = GreenTrack,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }
        }

        LaunchedEffect(Unit) {
            savedStateHandle?.getLiveData<Pair<Double, Double>>("location")?.observeForever { newLatLng ->
                latLng = newLatLng
                Toast.makeText(context, "Ubicación: ${newLatLng.first}, ${newLatLng.second}", Toast.LENGTH_SHORT).show()
            }
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                if (name.isNotBlank() && description.isNotBlank() && latLng != null && email.isNotBlank() && password.isNotBlank()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: return@addOnSuccessListener
                            val store = hashMapOf(
                                "id" to uid,
                                "name" to name,
                                "description" to description,
                                "locationDesc" to locationDesc,
                                "openHour" to openHour,
                                "closeHour" to closeHour,
                                "lat" to latLng!!.first,
                                "lng" to latLng!!.second,
                                "email" to email,
                                "type" to "store",
                                "daysOpen" to daysOpen.filterValues { it }
                            )
                            db.collection("stores").document(uid).set(store)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Tienda registrada exitosamente", Toast.LENGTH_SHORT).show()
                                    navController.navigate("store_dashboard/$uid") // Redirige al panel
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al guardar la tienda", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar tienda", color = Color.White)
        }

        Spacer(modifier = Modifier.height(48.dp)) // 👈 Espacio para que se vea el botón
    }
}

@Composable
fun TimePickerField(label: String, time: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current

    OutlinedTextField(
        value = time,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                val cal = Calendar.getInstance()
                val picker = TimePickerDialog(
                    context,
                    { _: TimePicker, hour: Int, minute: Int ->
                        onTimeSelected(String.format("%02d:%02d", hour, minute))
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                )
                picker.show()
            }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora")
            }
        }
    )
}

