package fr.epf.sin2.applicationvelib

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.epf.sin2.applicationvelib.model.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvName: TextView = itemView.findViewById(R.id.name_station_textview)
    val tvBikes: TextView = itemView.findViewById(R.id.num_bikes_available_textview)
    val tvDocks: TextView = itemView.findViewById(R.id.num_docks_available_textview)
    val ivStar : ImageView = itemView.findViewById(R.id.favorite_inlist_imageview)
}
class StationAdapter(val stations : List<Station>,
                     private val viewModel: FavoriteViewModel,
                    private val scope: CoroutineScope
) : RecyclerView.Adapter<StationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view =
            inflater.inflate(R.layout.station_view,parent,false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(vh: StationViewHolder, position: Int) {
        val station = stations[position] // stations.get(position)

        vh.tvName.text  = station.name
        vh.tvBikes.text = " ${station.numBikesAvailable} vélos"
        vh.tvDocks.text = " ${station.numDocksAvailable} places"

        scope.launch {
            val fav = viewModel.isFavorite(station.stationId)
            withContext(Dispatchers.Main) {
                vh.ivStar.setImageResource(
                    if (fav) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off
                )
            }
        }

        vh.ivStar.setImageResource(
            if (station.isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        vh.ivStar.setOnClickListener {
            station.isFavorite = !station.isFavorite
            vh.ivStar.setImageResource(
                if (station.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            viewModel.toggleFavorite(station)
        }

        vh.itemView.setOnClickListener {
            val intent = Intent(vh.itemView.context, DetailStationActivity::class.java).apply {
                putExtra(DetailStationActivity.EXTRA_STATION_ID, station.stationId)
                putExtra(DetailStationActivity.EXTRA_NAME,  station.name)
                putExtra(DetailStationActivity.EXTRA_LAT,   station.lat)
                putExtra(DetailStationActivity.EXTRA_LON,   station.lon)
                putExtra(DetailStationActivity.EXTRA_BIKES, station.numBikesAvailable)
                putExtra(DetailStationActivity.EXTRA_DOCKS, station.numDocksAvailable)
                putExtra(DetailStationActivity.EXTRA_MECA,  station.mechanicalBikes)
                putExtra(DetailStationActivity.EXTRA_ELEC,  station.eBikes)
            }
            vh.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount() = stations.size
}

private const val TAG = "StationAdapter"
