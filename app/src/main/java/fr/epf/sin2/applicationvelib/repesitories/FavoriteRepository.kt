package fr.epf.sin2.applicationvelib.repesitories

import android.content.Context
import fr.epf.sin2.applicationvelib.database.AppDatabase
import fr.epf.sin2.applicationvelib.model.Station
import fr.epf.sin2.applicationvelib.model.StationFavorite
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).favoriteDao()

    val favorites: Flow<List<StationFavorite>> = dao.getAllFavorites()

    suspend fun getFavoriteIds(): Set<String> =
        dao.getAllFavoritesOnce().map { it.stationId }.toSet()

    suspend fun toggleFavorite(station: Station) {
        if (dao.isFavorite(station.stationId)) {
            dao.delete(station.toFavorite())
        } else {
            dao.insert(station.toFavorite())
        }
    }

    suspend fun isFavorite(stationId: String) = dao.isFavorite(stationId)
}

// Extension pour convertir Station → StationFavorite
fun Station.toFavorite() = StationFavorite(
    stationId         = stationId,
    name              = name,
    lat               = lat,
    lon               = lon,
    capacity          = capacity,
    numBikesAvailable = numBikesAvailable,
    numDocksAvailable = numDocksAvailable,
    mechanicalBikes   = mechanicalBikes,
    eBikes            = eBikes
)