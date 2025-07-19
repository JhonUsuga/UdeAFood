package co.edu.udea.compumovil.gr07_20251.udeafood.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val storeId: String = "", // para saber a qué tienda pertenece

    val latestComment: String = "",   // Comentario más reciente o destacado
    val latestReviewer: String = "",
    val favoritedBy: List<String> = emptyList()
)