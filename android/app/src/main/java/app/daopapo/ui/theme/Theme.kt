package app.daopapo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta do Dá o Papo
// Azul royal / ciano: confiança e segurança sem parecer distante.
// Verde vivo: ganho, "pode ir", solução prática.
object Marca {
    val Royal = Color(0xFF2952E3)
    val RoyalEscuro = Color(0xFF1A36A8)
    val Ciano = Color(0xFF0096C7)
    val Verde = Color(0xFF16C26A)
    val VerdeTexto = Color(0xFF0B7A3E) // verde mais fechado para texto em fundo claro (contraste)
}

private val Claro = lightColorScheme(
    primary = Marca.Royal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE5FF),
    onPrimaryContainer = Color(0xFF0A1F66),
    secondary = Marca.Ciano,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDF1FF),
    onSecondaryContainer = Color(0xFF003547),
    tertiary = Marca.VerdeTexto,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC6F6D8),
    onTertiaryContainer = Color(0xFF00391A),
    background = Color(0xFFF6F8FF),
    onBackground = Color(0xFF121726),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121726),
    surfaceVariant = Color(0xFFE5EAF6),
    onSurfaceVariant = Color(0xFF444B5E),
    surfaceContainer = Color(0xFFEEF2FC),
    surfaceContainerHigh = Color(0xFFE6EBF8),
    outline = Color(0xFF747B8E),
    error = Color(0xFFBA1A1A),
)

private val Escuro = darkColorScheme(
    primary = Color(0xFF9DB4FF),
    onPrimary = Color(0xFF0A1F66),
    primaryContainer = Color(0xFF2341B8),
    onPrimaryContainer = Color(0xFFDDE5FF),
    secondary = Color(0xFF5FD4FF),
    onSecondary = Color(0xFF00354A),
    secondaryContainer = Color(0xFF004C66),
    onSecondaryContainer = Color(0xFFCDF1FF),
    tertiary = Color(0xFF3EE68A),
    onTertiary = Color(0xFF00391A),
    tertiaryContainer = Color(0xFF00692F),
    onTertiaryContainer = Color(0xFFC6F6D8),
    background = Color(0xFF0B1020),
    onBackground = Color(0xFFE6E9F2),
    surface = Color(0xFF121829),
    onSurface = Color(0xFFE6E9F2),
    surfaceVariant = Color(0xFF242C42),
    onSurfaceVariant = Color(0xFFC3C8D8),
    surfaceContainer = Color(0xFF171E31),
    surfaceContainerHigh = Color(0xFF1E263B),
    outline = Color(0xFF8C93A6),
    error = Color(0xFFFFB4AB),
)

// Letras um pouco maiores que o padrão: leitura mais fácil para todo mundo.
private val Tipos = Typography().let { t ->
    t.copy(
        bodyLarge = t.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),
        bodyMedium = t.bodyMedium.copy(fontSize = 16.sp, lineHeight = 23.sp),
        labelLarge = t.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
        titleMedium = t.titleMedium.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
    )
}

enum class Tema { SISTEMA, CLARO, ESCURO }

@Composable
fun DaOPapoTheme(tema: Tema = Tema.SISTEMA, content: @Composable () -> Unit) {
    val escuro = when (tema) {
        Tema.SISTEMA -> isSystemInDarkTheme()
        Tema.CLARO -> false
        Tema.ESCURO -> true
    }
    MaterialTheme(colorScheme = if (escuro) Escuro else Claro, typography = Tipos, content = content)
}
