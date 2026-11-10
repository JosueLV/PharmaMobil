package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val timestamp: String? = null,
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val validationErrors: Map<String, String>? = null
)