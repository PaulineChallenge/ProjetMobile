package fr.epf.sin2.applicationvelib.services

import fr.epf.sin2.applicationvelib.model.Station
import fr.epf.sin2.applicationvelib.model.StationInfoResponse
import fr.epf.sin2.applicationvelib.model.StationStatusResponse
import retrofit2.http.GET

interface StationService {
    @GET("station_information.json")
    suspend fun getStationsInformation(): StationInfoResponse

    @GET("station_status.json")
    suspend fun getStationsStatus(): StationStatusResponse
}