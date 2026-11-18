package com.colora.world.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colora.world.R

/** Design tokens from spec §3.2 / docs/ART_DIRECTION.md. Screens never hard-code colours. */
object C {
    val bgCream = Color(0xFFFFF8EF)
    val paper = Color(0xFFFFFFFF)
    val ink = Color(0xFF24324B)
    val inkSoft = Color(0xFF5A6781)
    val coral = Color(0xFFFF786D)
    val coralDeep = Color(0xFFE95F55)
    val turquoise = Color(0xFF42C8BD)
    val sunshine = Color(0xFFFFD66B)
    val lavender = Color(0xFFB8A0F3)
    val sky = Color(0xFF8BD4F6)
    val leaf = Color(0xFF9DDC86)
    val rose = Color(0xFFFFE6E0)
    val focus = Color(0xFF34548D)
    val shadow = Color(0x2624324B)
    val line = Color(0xFFEFE3D3)
    val sand = Color(0xFFFCEFDC)
    val mint = Color(0xFFDDF5F1)
    val lilac = Color(0xFFEDE6FF)
    val butter = Color(0xFFFFF1C7)
    val skyPale = Color(0xFFE2F4FD)
}

object D {
    val touch = 56.dp
    val primary = 72.dp
    val cardRadius = 24.dp
    val panelRadius = 30.dp
    val buttonRadius = 22.dp
    val gap = 12.dp
    val gutter = 16.dp
}

val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

object T {
    val hero = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, color = C.ink)
    val title = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp, color = C.ink)
    val heading = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp, color = C.ink)
    val label = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 23.sp, color = C.ink)
    val small = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp, color = C.ink)
    val body = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp, color = C.ink)
    val parent = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, color = C.ink)
}

@Composable
fun ColoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.coral, onPrimary = C.ink, secondary = C.turquoise, background = C.bgCream, surface = C.paper,
            onSurface = C.ink, onBackground = C.ink, surfaceVariant = C.sand, outline = C.line,
        ),
        typography = Typography(
            bodyLarge = T.body, bodyMedium = T.parent, titleLarge = T.title, labelLarge = T.label,
        ),
        content = content,
    )
}
