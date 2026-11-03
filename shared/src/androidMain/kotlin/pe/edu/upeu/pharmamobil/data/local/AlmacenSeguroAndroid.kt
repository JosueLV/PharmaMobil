package pe.edu.upeu.pharmamobil.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {

    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        contexto,
        "sesion_segura",
        llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) =
        prefs.edit().putString(CLAVE, token).apply()

    override fun leerToken(): String? = prefs.getString(CLAVE, null)

    override fun borrarToken() =
        prefs.edit().remove(CLAVE).apply()

    override fun guardarSesion(sesion: String) =
        prefs.edit().putString(CLAVE_SESION, sesion).apply()

    override fun leerSesion(): String? = prefs.getString(CLAVE_SESION, null)

    override fun borrarSesion() =
        prefs.edit().remove(CLAVE_SESION).apply()

    private companion object {
        const val CLAVE = "jwt"
        const val CLAVE_SESION = "ultima_sesion_validada"
    }
}
