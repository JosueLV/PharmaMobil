package pe.edu.upeu.pharmamobil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores corporativos PharmaMobil
private val PharmaGreen = Color(0xFF00695C)
private val PharmaGreenLight = Color(0xFFB2DFDB)
private val PharmaGreenDark = Color(0xFF004D40)
private val PharmaAccent = Color(0xFF00BFA5)

private val LightColors = lightColorScheme(
    primary = PharmaGreen,
    onPrimary = Color.White,
    primaryContainer = PharmaGreenLight,
    onPrimaryContainer = PharmaGreenDark,
    secondary = PharmaAccent,
    onSecondary = Color.White,
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF1B1B1B),
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF1B1B1B),
)

private val DarkColors = darkColorScheme(
    primary = PharmaAccent,
    onPrimary = Color(0xFF00201C),
    primaryContainer = PharmaGreenDark,
    onPrimaryContainer = PharmaGreenLight,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF00201C),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
)

@Composable
fun PharmaMobilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}