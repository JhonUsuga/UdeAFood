package co.edu.udea.compumovil.gr07_20251.udeafood.model

data class Review(
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
