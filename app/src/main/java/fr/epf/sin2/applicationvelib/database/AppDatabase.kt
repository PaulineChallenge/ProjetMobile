package fr.epf.sin2.applicationvelib.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import fr.epf.sin2.applicationvelib.dao.FavoriteDao
import fr.epf.sin2.applicationvelib.model.StationFavorite

@Database(entities = [StationFavorite::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velib_db"
                ).build().also { instance = it }
            }
    }
}