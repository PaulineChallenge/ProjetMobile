package fr.epf.sin2.applicationvelib.model

import com.google.gson.annotations.SerializedName

// --- station_information.json ---

data class StationInfoResponse(
    val data: StationInfoData
)

data class StationInfoData(
    val stations: List<StationInfo>
)

data class StationInfo(
    @SerializedName("station_id") val stationId: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int
)