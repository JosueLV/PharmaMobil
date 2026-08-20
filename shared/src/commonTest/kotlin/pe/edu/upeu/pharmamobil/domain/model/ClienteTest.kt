package pe.edu.upeu.pharmamobil.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest{

    @Suppress("SuspiciousIndentation")
    @Test
    fun probarCliente(){

    val cliente = Cliente(
        id = 1L,
        nombre = "Farmacia Nueva",
        correo = "ventas@central.pe",
        telefono = "987654123"
    )
    val resultado = cliente.telefono

        assertEquals(
            expected = "987654123",
            actual = resultado
        )
}
}
