package pe.edu.upeu.pharmamobil.data.local

import pe.edu.upeu.pharmamobil.db.PharmaDb
import app.cash.sqldelight.db.SqlDriver

expect fun driverEnMemoria(): SqlDriver

fun baseEnMemoria(): PharmaDb = PharmaDb(driverEnMemoria())
