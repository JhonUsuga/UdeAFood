package co.edu.udea.compumovil.gr07_20251.udeafood.ui.screens

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import co.edu.udea.compumovil.gr07_20251.udeafood.R

@Composable
fun MapPickerScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }

    Column(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                val map = MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
                    controller.setCenter(GeoPoint(6.2663, -75.5688)) // Universidad de Antioquia
                }

                map.overlays.clear()

                map.setOnTouchListener { v, event ->
                    val projection = map.projection
                    val point = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                    selectedPoint = point

                    map.overlays.clear()

                    val marker = Marker(map).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ctx.getDrawable(R.drawable.baseline_location_on_24)
                        title = "Ubicación seleccionada"
                    }

                    map.overlays.add(marker)
                    map.invalidate()

                    map.invalidate()
                    false
                }

                map
            }
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                if (selectedPoint != null) {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("location", Pair(selectedPoint!!.latitude, selectedPoint!!.longitude))
                    navController.popBackStack()
                } else {
                    Toast.makeText(context, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Confirmar ubicación")
        }
    }
}