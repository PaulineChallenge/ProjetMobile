package fr.epf.sin2.applicationvelib.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class StationFavorite(
    @PrimaryKey val stationId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val numBikesAvailable: Int,
    val numDocksAvailable: Int,
    val mechanicalBikes: Int,
    val eBikes: Int
)