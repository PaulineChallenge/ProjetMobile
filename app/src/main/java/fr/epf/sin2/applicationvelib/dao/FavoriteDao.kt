package fr.epf.sin2.applicationvelib.dao

import androidx.room.*
import fr.epf.sin2.applicationvelib.model.StationFavorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<StationFavorite>>

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavoritesOnce(): List<StationFavorite>

    @Query("SELECT * FROM favorites WHERE stationId = :id")
    suspend fun getById(id: String): StationFavorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: StationFavorite)

    @Delete
    suspend fun delete(station: StationFavorite): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE stationId = :id)")
    suspend fun isFavorite(id: String): Boolean
}