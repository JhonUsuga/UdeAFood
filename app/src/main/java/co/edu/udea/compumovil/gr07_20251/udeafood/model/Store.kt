package co.edu.udea.compumovil.gr07_20251.udeafood.model

data class Store(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val type: String = "store", // o "user"
    val category: String = "", // Ej: "Burgers", "Cafe"
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isFeatured: Boolean = false,
    val isOpen: Boolean = true,
    val openHour: String = "09:00",
    val closeHour: String = "21:00",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)