package fr.epf.sin2.applicationvelib

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import fr.epf.sin2.applicationvelib.model.Station
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class DetailStationActivity : AppCompatActivity() {

    private lateinit var miniMapView: MapView
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var ivStar: ImageView
    private var stationId: String = ""

    companion object {
        const val EXTRA_STATION_ID = "extra_station_id"
        const val EXTRA_NAME       = "extra_name"
        const val EXTRA_LAT        = "extra_lat"
        const val EXTRA_LON        = "extra_lon"
        const val EXTRA_BIKES      = "extra_bikes"
        const val EXTRA_DOCKS      = "extra_docks"
        const val EXTRA_MECA       = "extra_meca"
        const val EXTRA_ELEC       = "extra_elec"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_detail_station)

        favoriteViewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        ivStar = findViewById(R.id.favorite_indetail_imageview)

        // Récupérer les données
        stationId  = intent.getStringExtra(EXTRA_STATION_ID) ?: ""
        val name   = intent.getStringExtra(EXTRA_NAME) ?: ""
        val lat    = intent.getDoubleExtra(EXTRA_LAT, 48.8566)
        val lon    = intent.getDoubleExtra(EXTRA_LON, 2.3522)
        val bikes  = intent.getIntExtra(EXTRA_BIKES, 0)
        val docks  = intent.getIntExtra(EXTRA_DOCKS, 0)
        val meca   = intent.getIntExtra(EXTRA_MECA, 0)
        val elec   = intent.getIntExtra(EXTRA_ELEC, 0)

        // Remplir les TextViews
        findViewById<TextView>(R.id.station_name_textview).text             = name
        findViewById<TextView>(R.id.station_nbr_bike_textview).text         = "🚲 Vélos disponibles : $bikes"
        findViewById<TextView>(R.id.station_nbr_dock_textview).text         = "🅿️ Places libres : $docks"
        findViewById<TextView>(R.id.station_nbr_bike_meca_textview).text    = "⚙️ Mécaniques : $meca"
        findViewById<TextView>(R.id.station_nbr_bike_elec_textview).text    = "⚡ Électriques : $elec"

        // Lire l'état favori depuis Room (source de vérité)
        rafraichirEtoile()

        // Toggle au clic
        ivStar.setOnClickListener {
            val station = Station(
                stationId         = stationId,
                name              = name,
                lat               = lat,
                lon               = lon,
                capacity          = 0,
                isInstalled       = true,
                isRenting         = true,
                isReturning       = true,
                numBikesAvailable = bikes,
                numDocksAvailable = docks,
                mechanicalBikes   = meca,
                eBikes            = elec,
                isFavorite        = false
            )
            favoriteViewModel.toggleFavorite(station)
            rafraichirEtoile()
        }

        // Mini carte
        miniMapView = findViewById(R.id.miniMapView)
        miniMapView.setTileSource(TileSourceFactory.MAPNIK)
        miniMapView.setMultiTouchControls(true)

        val point = GeoPoint(lat, lon)
        miniMapView.controller.setZoom(16.0)
        miniMapView.controller.setCenter(point)

        val marker = Marker(miniMapView)
        marker.position = point
        marker.title = name
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        miniMapView.overlays.add(marker)

        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    // Fonction pour mettre à jour l'étoile
    private fun rafraichirEtoile() {
        lifecycleScope.launch {
            val isFav = favoriteViewModel.isFavorite(stationId)
            ivStar.setImageResource(
                if (isFav) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

    override fun onResume() {
        super.onResume()
        miniMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        miniMapView.onPause()
    }
}