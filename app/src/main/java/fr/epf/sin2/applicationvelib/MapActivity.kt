package fr.epf.sin2.applicationvelib

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.epf.sin2.applicationvelib.model.Station
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.lifecycle.Lifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.views.overlay.infowindow.InfoWindow
import android.Manifest
import android.location.Location
import android.widget.TextView
import com.google.android.gms.location.LocationServices

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val viewModel = SharedViewModelProvider.stationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001
    private var modeVelos = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        setupMap()

        mapView.setOnTouchListener { _, _ ->
            InfoWindow.closeAllInfoWindowsOn(mapView)
            false
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        demanderPermissionEtCentrer()

        observeStations()
        viewModel.loadStations()

        findViewById<FloatingActionButton>(R.id.fabList).setOnClickListener {
            startActivity(Intent(this, ListStationsActivity::class.java))
            finish()
        }

        findViewById<FloatingActionButton>(R.id.fabRefresh).setOnClickListener {
            viewModel.loadStations()
        }

        // fabCenter en dehors de fabToggle
        findViewById<FloatingActionButton>(R.id.fabCenter).setOnClickListener {
            centrerSurPosition()
        }

        findViewById<FloatingActionButton>(R.id.fabToggle).setOnClickListener {
            modeVelos = !modeVelos
            val label = if (modeVelos) "🚲 Couleur selon vélos" else "🅿️ Couleur selon places"
            findViewById<TextView>(R.id.tvMode).text = label
            // Redessiner avec le mode actuel
            val state = viewModel.uiState.value
            if (state is StationModelView.UiState.Success) {
                afficherMarqueurs(state.stations)
            }
        }
    }

    private fun demanderPermissionEtCentrer() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            centrerSurPosition()
        } else {
            // Demander la permission à l'utilisateur
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    private fun centrerSurPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userPosition = GeoPoint(it.latitude, it.longitude)
                mapView.controller.animateTo(userPosition)
                mapView.controller.setZoom(18.0)

                // Marqueur position actuelle
                val marker = Marker(mapView)
                marker.position = userPosition
                marker.title = "Ma position"
                marker.icon = resources.getDrawable(android.R.drawable.ic_menu_mylocation, theme)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(marker)
                mapView.invalidate()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            centrerSurPosition()
        }
    }


    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
    }

    private fun observeStations() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is StationModelView.UiState.Loading -> {
                        }
                        is StationModelView.UiState.Success -> {
                            afficherMarqueurs(state.stations)
                        }
                        is StationModelView.UiState.Error -> {
                            Toast.makeText(this@MapActivity, state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun afficherMarqueurs(stations: List<Station>) {
        val infoWindow = CustomInfoWindow(mapView, this)
        mapView.overlays.clear()

        stations.forEach { station ->
            val marker = Marker(mapView)
            marker.position = GeoPoint(station.lat, station.lon)
            marker.title = station.name
            marker.snippet = buildString {
                append("🚲 ${station.numBikesAvailable} vélos")
                append(" (${station.mechanicalBikes} méca, ${station.eBikes} élec)")
                append("\n🅿️ ${station.numDocksAvailable} places libres")
            }

            marker.relatedObject = station

            marker.infoWindow = infoWindow
            marker.setOnMarkerClickListener { clickedMarker, _ ->
                infoWindow.setStation(clickedMarker.relatedObject as Station)
                clickedMarker.showInfoWindow()
                true
            }

            marker.icon = if (modeVelos) {
                when {
                    station.numBikesAvailable == 0  -> resources.getDrawable(android.R.drawable.presence_busy, theme)
                    station.numBikesAvailable <= 2  -> resources.getDrawable(android.R.drawable.presence_away, theme)
                    else                            -> resources.getDrawable(android.R.drawable.presence_online, theme)
                }
            } else {
                when {
                    station.numDocksAvailable == 0  -> resources.getDrawable(android.R.drawable.presence_busy, theme)
                    station.numDocksAvailable <= 2  -> resources.getDrawable(android.R.drawable.presence_away, theme)
                    else                            -> resources.getDrawable(android.R.drawable.presence_online, theme)
                }
            }

            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)  // ✅ Une seule fois
        }

        mapView.invalidate()
    }

    // Cycle de vie
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}