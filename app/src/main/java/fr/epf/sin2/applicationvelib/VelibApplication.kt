package fr.epf.sin2.applicationvelib

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import fr.epf.sin2.applicationvelib.repesitories.StationRepository

class VelibApplication : Application(), ViewModelStoreOwner {

    private val appViewModelStore = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = appViewModelStore

    companion object {
        lateinit var instance: VelibApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        StationRepository.getInstance(this)
    }
}