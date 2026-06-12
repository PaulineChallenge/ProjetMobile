package fr.epf.sin2.applicationvelib.repesitories

import fr.epf.sin2.applicationvelib.network.StationRetrofitClient
import fr.epf.sin2.applicationvelib.services.StationService
import fr.epf.sin2.applicationvelib.model.Station
import fr.epf.sin2.applicationvelib.model.StationInfo
import fr.epf.sin2.applicationvelib.model.StationStatus

class StationRepository(
    private val api: StationService = StationRetrofitClient.api,
    private val favoriteRepository: FavoriteRepository? = null
) {
    private var cachedInfoList: List<StationInfo>? = null
    private var cachedStatusMap: Map<String, StationStatus> = emptyMap()
    private var lastStatusRefresh: Long = 0
    private val STATUS_REFRESH_INTERVAL = 60_000

    // Appels parallèles avec async/await
    suspend fun getStations(): List<Station> {
            // Info statiques — chargées une seule fois
            if (cachedInfoList == null) {
                cachedInfoList = api.getStationsInformation().data.stations
            }

            // Status — rechargés seulement si > 1 minute
            val now = System.currentTimeMillis()
            if (now - lastStatusRefresh > STATUS_REFRESH_INTERVAL) {
                lastStatusRefresh = now
                cachedStatusMap = api.getStationsStatus()
                    .data.stations
                    .associateBy { it.stationId }
            }

            val favoriteIds = favoriteRepository?.getFavoriteIds() ?: emptySet()

            return cachedInfoList!!.mapNotNull { info ->
                val status = cachedStatusMap[info.stationId] ?: return@mapNotNull null
                Station(
                    stationId = info.stationId,
                    name = info.name,
                    lat = info.lat,
                    lon = info.lon,
                    capacity = info.capacity,
                    isInstalled = status.isInstalled == 1,
                    isRenting = status.isRenting == 1,
                    isReturning = status.isReturning == 1,
                    numBikesAvailable = status.numBikesAvailable,
                    numDocksAvailable = status.numDocksAvailable,
                    mechanicalBikes = status.numBikesAvailableTypes
                        ?.firstOrNull { it.containsKey("mechanical") }
                        ?.get("mechanical") ?: 0,
                    eBikes = status.numBikesAvailableTypes
                        ?.firstOrNull { it.containsKey("ebike") }
                        ?.get("ebike") ?: 0,
                    isFavorite = info.stationId in favoriteIds                )
            }
        }

    companion object {
        //L'instance a besoin du context pour Room
        fun getInstance(context: android.content.Context): StationRepository {
            return instance ?: StationRepository(
                favoriteRepository = FavoriteRepository(context)
            ).also { instance = it }
        }

        private var instance: StationRepository? = null
    }

    // Filtrer uniquement les stations installées et actives
    suspend fun getActiveStations(): List<Station> =
        getStations().filter { it.isInstalled && it.isRenting }

    //Filtre les stations favorites
    suspend fun getActiveFavoriteStations(): List<Station> =
        getActiveStations().filter { it.isFavorite }

    fun invalideStatus() {
        lastStatusRefresh = 0
    }

    /*companion object {
        val instance: StationRepository by lazy { StationRepository() }
    }*/
    }