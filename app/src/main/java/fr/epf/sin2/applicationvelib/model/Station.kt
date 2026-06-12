package fr.epf.sin2.applicationvelib.model

// --- Modèle fusionné avec favori ---

data class Station(
    val stationId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val isInstalled: Boolean,
    val isRenting: Boolean,
    val isReturning: Boolean,
    val numBikesAvailable: Int,
    val numDocksAvailable: Int,
    val mechanicalBikes: Int,
    val eBikes: Int,
    var isFavorite: Boolean
)