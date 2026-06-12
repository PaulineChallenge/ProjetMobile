package fr.epf.sin2.applicationvelib.model

import com.google.gson.annotations.SerializedName

// --- station_status.json ---

data class StationStatusResponse(
    val data: StationStatusData
)

data class StationStatusData(
    val stations: List<StationStatus>
)

data class StationStatus(
    @SerializedName("station_id") val stationId: String,
    @SerializedName("is_installed") val isInstalled: Int,
    @SerializedName("is_renting") val isRenting: Int,
    @SerializedName("is_returning") val isReturning: Int,
    @SerializedName("num_bikes_available") val numBikesAvailable: Int,
    @SerializedName("num_docks_available") val numDocksAvailable: Int,
    @SerializedName("num_bikes_available_types") val numBikesAvailableTypes: List<Map<String, Int>>?= null
)
