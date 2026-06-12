package fr.epf.sin2.applicationvelib

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.epf.sin2.applicationvelib.model.Station
import fr.epf.sin2.applicationvelib.repesitories.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StationModelView(
    private val repository : StationRepository = StationRepository.getInstance(VelibApplication.instance)
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val stations: List<Station>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    fun loadStations() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            Log.d("Velib", "→ loadStations() lancé")
            try {
                val stations = repository.getActiveStations()
                Log.d("Velib", "✅ UiState.Success avec ${stations.size} stations")
                _uiState.value = UiState.Success(stations)
            } catch (e: Exception) {
                Log.e("Velib", "❌ Erreur : ${e.message}")
                _uiState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }
}