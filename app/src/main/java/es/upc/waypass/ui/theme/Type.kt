package es.upc.waypass.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Font family ─────────────────────────────────────────────────────────────
// Asegúrate de tener en res/font/:
//   poppins_regular.ttf  (weight 400)
//   poppins_medium.ttf   (weight 500)
//   poppins_semibold.ttf (weight 600)
// Descarga desde https://fonts.google.com/specimen/Poppins
val Poppins = FontFamily(
    Font(es.upc.waypass.R.font.poppins_regular,  FontWeight.Normal),
    Font(es.upc.waypass.R.font.poppins_medium,   FontWeight.Medium),
    Font(es.upc.waypass.R.font.poppins_semibold, FontWeight.SemiBold),
)

// ─── Typography scale (mapeo 1:1 con Stitch) ─────────────────────────────────
//
//  Stitch token       → M3 slot           size  weight  lineHeight
//  headline-lg        → headlineLarge      28sp  600     36sp
//  headline-md        → headlineMedium     22sp  600     28sp
//  title-lg           → titleLarge         18sp  500     24sp
//  title-md           → titleMedium        16sp  500     24sp
//  body-lg            → bodyLarge          16sp  400     24sp
//  body-md            → bodyMedium         14sp  400     20sp
//  label-lg           → labelLarge         12sp  500     16sp
//  label-sm           → labelSmall         11sp  500     16sp
//
val WayPassTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),
    // headlineSmall no está en Stitch — lo dejamos cercano al sistema
    headlineSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    // titleSmall — utilitario, no definido en Stitch
    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
    ),
)
