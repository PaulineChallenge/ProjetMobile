package fr.epf.sin2.applicationvelib

import android.content.Context
import android.content.Intent
import android.widget.TextView
import fr.epf.sin2.applicationvelib.model.Station
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(
    mapView: MapView,
    private val context: Context
) : InfoWindow(R.layout.custom_bubble, mapView) {

    private var currentStation: Station? = null

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        mView.findViewById<TextView>(R.id.bubble_title).text = marker.title
        mView.findViewById<TextView>(R.id.bubble_description).text = marker.snippet

        // Clic sur la bulle → ouvre StationDetailActivity
        mView.setOnClickListener {
            currentStation?.let { station ->
                val intent = Intent(context, DetailStationActivity::class.java).apply {
                    putExtra(DetailStationActivity.EXTRA_NAME,  station.name)
                    putExtra(DetailStationActivity.EXTRA_LAT,   station.lat)
                    putExtra(DetailStationActivity.EXTRA_LON,   station.lon)
                    putExtra(DetailStationActivity.EXTRA_BIKES, station.numBikesAvailable)
                    putExtra(DetailStationActivity.EXTRA_DOCKS, station.numDocksAvailable)
                    putExtra(DetailStationActivity.EXTRA_MECA,  station.mechanicalBikes)
                    putExtra(DetailStationActivity.EXTRA_ELEC,  station.eBikes)
                }
                context.startActivity(intent)
            }
            close()
        }
    }

    fun setStation(station: Station) {
        currentStation = station
    }

    override fun onClose() {}
}