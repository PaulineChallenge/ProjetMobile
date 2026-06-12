package fr.epf.sin2.applicationvelib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.epf.sin2.applicationvelib.model.Station
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var viewModel: FavoriteViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        viewModel = ViewModelProvider(this)[FavoriteViewModel::class.java]
        recyclerView = findViewById(R.id.rvFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favorites.collect { favorites ->
                    val stations = favorites.map { fav ->
                        Station(
                            stationId = fav.stationId,
                            name = fav.name,
                            lat = fav.lat,
                            lon = fav.lon,
                            capacity = fav.capacity,
                            isInstalled = true,
                            isRenting = true,
                            isReturning = true,
                            numBikesAvailable = fav.numBikesAvailable,
                            numDocksAvailable = fav.numDocksAvailable,
                            mechanicalBikes = fav.mechanicalBikes,
                            eBikes = fav.eBikes,
                            isFavorite = true
                        )
                    }
                    recyclerView.adapter = StationAdapter(stations, viewModel, lifecycleScope)
                }
            }
        }

        findViewById<FloatingActionButton>(R.id.fabBack).setOnClickListener {
            finish()
        }
    }
}