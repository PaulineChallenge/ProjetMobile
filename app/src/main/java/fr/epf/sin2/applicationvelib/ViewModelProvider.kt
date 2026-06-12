package fr.epf.sin2.applicationvelib

import androidx.lifecycle.ViewModelProvider

object SharedViewModelProvider {
    val stationViewModel: StationModelView by lazy {
        ViewModelProvider(VelibApplication.instance)[StationModelView::class.java]
    }
}