package fr.epf.sin2.applicationvelib

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.epf.sin2.applicationvelib.model.Station
import fr.epf.sin2.applicationvelib.model.StationFavorite
import fr.epf.sin2.applicationvelib.repesitories.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteViewModel(app: Application) : AndroidViewModel(app) {

    private var favoriteViewModel : FavoriteViewModel = this

    private val repository = FavoriteRepository(app)

    val favorites: StateFlow<List<StationFavorite>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleFavorite(station: Station) {
        viewModelScope.launch {
            repository.toggleFavorite(station)
        }
    }

    suspend fun isFavorite(stationId: String) =
        repository.isFavorite(stationId)

    suspend fun getFavoriteIds(): Set<String> {
        return favoriteViewModel.favorites.value
            .map { it.stationId }
            .toSet()
    }
}
