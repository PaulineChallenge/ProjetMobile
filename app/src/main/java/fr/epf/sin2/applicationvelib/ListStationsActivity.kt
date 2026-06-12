package fr.epf.sin2.applicationvelib

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.epf.sin2.applicationvelib.model.Station
import kotlinx.coroutines.launch
import kotlin.math.pow

class ListStationsActivity : AppCompatActivity() {
    private lateinit  var recyclerview : RecyclerView
    private val viewModel = SharedViewModelProvider.stationViewModel
    private lateinit var favoriteViewModel : FavoriteViewModel
    private var allStations: List<Station> = emptyList()
    private var userLat: Double? = null
    private var userLon: Double? = null
    private var showFavoritesOnly = false

    enum class SortMode { ALPHA, DISTANCE, BIKES, DOCKS }
    private var currentSort = SortMode.ALPHA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_stations)

        recupererPosition()

        //viewModel = ViewModelProvider(this)[StationModelView::class.java]
        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]

        recyclerview = findViewById<RecyclerView>(R.id.clients_recyclerview)
        recyclerview.apply {
            layoutManager =
                LinearLayoutManager(this@ListStationsActivity,
                    LinearLayoutManager.VERTICAL,false)

        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is StationModelView.UiState.Success) {
                        allStations = state.stations
                        appliquerTriEtFiltre()
                    }
                }
            }
        }

        if (viewModel.uiState.value !is StationModelView.UiState.Success) {
            viewModel.loadStations()
        }

        configurerBoutons()
    }


    private fun configurerBoutons() {

        findViewById<Button>(R.id.btnSortAlpha).setOnClickListener {
            currentSort = SortMode.ALPHA
            appliquerTriEtFiltre()
        }

        findViewById<Button>(R.id.btnSortDistance).setOnClickListener {
            if (userLat == null) {
                Toast.makeText(this, "Position GPS non disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentSort = SortMode.DISTANCE
            appliquerTriEtFiltre()
        }

        findViewById<Button>(R.id.btnSortBikes).setOnClickListener {
            currentSort = SortMode.BIKES
            appliquerTriEtFiltre()
        }

        findViewById<Button>(R.id.btnSortDocks).setOnClickListener {
            currentSort = SortMode.DOCKS
            appliquerTriEtFiltre()
        }

        findViewById<FloatingActionButton>(R.id.fabMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
            finish()
        }

        findViewById<FloatingActionButton>(R.id.fabFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }

    private fun appliquerTriEtFiltre() {
        lifecycleScope.launch {
            var stations =  allStations.toList()

            // Trier
            stations = when (currentSort) {
                SortMode.ALPHA -> stations.sortedBy { it.name }
                SortMode.BIKES -> stations.sortedByDescending { it.numBikesAvailable }
                SortMode.DOCKS -> stations.sortedByDescending { it.numDocksAvailable }
                SortMode.DISTANCE -> {
                    val lat = userLat ?: return@launch
                    val lon = userLon ?: return@launch
                    stations.sortedBy { distanceKm(lat, lon, it.lat, it.lon) }
                }
            }
            recyclerview.adapter = StationAdapter(stations, favoriteViewModel, lifecycleScope)
        }
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2) +
                Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2)
        return R * 2 * Math.asin(Math.sqrt(a))
    }

    private fun recupererPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this)
                .lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLat = it.latitude
                        userLon = it.longitude
                    }
                }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list_station,menu)
        return super.onCreateOptionsMenu(menu)
    }
}