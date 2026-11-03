package pe.edu.upeu.pharmamobil.data.local

interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
    fun guardarSesion(sesion: String)
    fun leerSesion(): String?
    fun borrarSesion()
}
