package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MigracionOfflineTest {
    @Test
    fun migracionDesdeVersionUno_conservaProductosYPendientes() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            driver.execute(null, """
                CREATE TABLE producto (
                    id INTEGER NOT NULL PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    precio REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    pendiente INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent(), 0)
            driver.execute(null, "INSERT INTO producto VALUES (-1, 'Pendiente', 4.5, 20, 1)", 0)
            driver.execute(null, "INSERT INTO producto VALUES (1, 'Remoto', 2.0, 50, 0)", 0)

            PharmaDb.Schema.migrate(driver, 1L, 2L)
            val base = PharmaDb(driver)
            val filas = base.productoQueries.listarTodos().executeAsList()

            assertEquals(2, filas.size)
            assertEquals(1L, filas.single { it.id == -1L }.pendiente)
            assertEquals(0L, filas.single { it.id == 1L }.pendiente)
            assertEquals(listOf(1L, 1L), filas.map { it.activo })
            assertNull(base.sincronizacionQueries.ultimaCorrecta().executeAsOneOrNull())
            base.sincronizacionQueries.guardarUltimaCorrecta(123_456L)
            assertEquals(123_456L, base.sincronizacionQueries.ultimaCorrecta().executeAsOne())
        } finally {
            driver.close()
        }
    }
}
