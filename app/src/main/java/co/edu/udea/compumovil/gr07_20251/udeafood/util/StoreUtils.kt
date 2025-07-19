package co.edu.udea.compumovil.gr07_20251.udeafood.util

import android.content.Context
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun isStoreOpen(openHour: String, closeHour: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val now = LocalTime.now()
    val open = LocalTime.parse(openHour, formatter)
    val close = LocalTime.parse(closeHour, formatter)

    return if (close.isAfter(open)) {
        now.isAfter(open) && now.isBefore(close)
    } else {
        // Caso: horario que cruza medianoche
        now.isAfter(open) || now.isBefore(close)
    }
}

// Guardar los IDs de productos ya vistos
fun saveKnownProductIds(context: Context, ids: Set<String>) {
    val prefs = context.getSharedPreferences("udeafood_prefs", Context.MODE_PRIVATE)
    prefs.edit().putStringSet("known_product_ids", ids).apply()
}

// Recuperar los IDs de productos ya vistos
fun getKnownProductIds(context: Context): Set<String> {
    val prefs = context.getSharedPreferences("udeafood_prefs", Context.MODE_PRIVATE)
    return prefs.getStringSet("known_product_ids", emptySet()) ?: emptySet()
}