package pe.edu.upeu.pharmamobil

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
            properties(mapOf(
                "pharma.urlBase" to BuildConfig.PHARMA_API_URL,
                "pharma.esDepuracion" to BuildConfig.DEBUG
            ))
        }
    }
}