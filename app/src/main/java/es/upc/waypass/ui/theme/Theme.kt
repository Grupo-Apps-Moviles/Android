package es.upc.waypass.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WayPassColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary     = InversePrimary,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary            = Tertiary,
    onTertiary          = OnTertiary,
    tertiaryContainer   = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error            = Error,
    onError          = OnError,
    errorContainer   = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    background   = Background,
    onBackground = OnBackground,

    surface                 = Surface,
    onSurface               = OnSurface,
    onSurfaceVariant        = OnSurfaceVariant,
    surfaceTint             = SurfaceTint,
    surfaceContainerLowest  = SurfaceContainerLowest,
    surfaceContainerLow     = SurfaceContainerLow,
    surfaceContainer        = SurfaceContainer,
    surfaceContainerHigh    = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceDim              = SurfaceDim,
    surfaceBright           = SurfaceBright,
    inverseSurface          = InverseSurface,
    inverseOnSurface        = InverseOnSurface,

    outline        = Outline,
    outlineVariant = OutlineVariant,
)

@Composable
fun WayPassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WayPassColorScheme,
        typography  = WayPassTypography,
        content     = content
    )
}
