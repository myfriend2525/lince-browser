package com.lince.browser.shared.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas

/** Animated "liquid metal" brush for the address-bar load outline. */
interface LiquidMetalBrushFactory {
    /** Runtime shaders only draw on hardware canvases; software capture must use a fallback. */
    fun supports(canvas: Canvas): Boolean

    /** Returns a brush for [timeSeconds] of shader time, or null when the shader is unavailable. */
    fun brush(timeSeconds: Float, darkSurface: Boolean): Brush?
}

/** Android supplies an AGSL runtime shader; platforms without one fall back to the tonal style. */
expect fun createLiquidMetalBrushFactory(): LiquidMetalBrushFactory?
