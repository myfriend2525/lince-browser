package com.lince.browser.ui

import android.graphics.Bitmap
import android.graphics.HardwareRenderer
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ShaderBrush
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lince.browser.shared.ui.createLiquidMetalBrushFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiquidMetalBrushInstrumentedTest {
    @Test
    fun liquidMetalShaderCompilesAndRendersVariedOpaquePixels() {
        val factory = createLiquidMetalBrushFactory()
        assertNotNull("AGSL liquid-metal shader must compile on the platform", factory)
        val pixels = render(factory!!.shader(timeSeconds = 1.5f, darkSurface = true, WIDTH, HEIGHT))
        val distinct = pixels.toSet().size
        assertTrue("Expected a metallic pattern, got $distinct colors", distinct > 50)
        assertTrue("Shader output must be opaque", pixels.all { (it ushr 24) == 0xFF })
    }

    @Test
    fun liquidMetalFramesChangeOverTime() {
        val factory = createLiquidMetalBrushFactory()!!
        val first = render(factory.shader(timeSeconds = 0f, darkSurface = false, WIDTH, HEIGHT))
        val later = render(factory.shader(timeSeconds = 2f, darkSurface = false, WIDTH, HEIGHT))
        assertFalse(first.contentEquals(later))
    }

    @Test
    fun softwareCanvasIsRejectedSoItCanFallBack() {
        val factory = createLiquidMetalBrushFactory()!!
        val software = Canvas(android.graphics.Canvas(Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)))
        assertFalse(factory.supports(software))
    }

    private fun com.lince.browser.shared.ui.LiquidMetalBrushFactory.shader(
        timeSeconds: Float,
        darkSurface: Boolean,
        width: Int,
        height: Int,
    ): Shader {
        val brush = brush(timeSeconds, darkSurface) as ShaderBrush
        return brush.createShader(Size(width.toFloat(), height.toFloat()))
    }

    /** RuntimeShader paints require a hardware canvas, as in the real address bar. */
    private fun render(shader: Shader): IntArray {
        val reader = ImageReader.newInstance(
            WIDTH,
            HEIGHT,
            PixelFormat.RGBA_8888,
            1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT,
        )
        val node = RenderNode("liquid-metal").apply { setPosition(0, 0, WIDTH, HEIGHT) }
        val canvas = node.beginRecording()
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), Paint().apply { this.shader = shader })
        node.endRecording()
        val renderer = HardwareRenderer().apply {
            setSurface(reader.surface)
            setContentRoot(node)
        }
        try {
            renderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()
            val image = checkNotNull(reader.acquireNextImage())
            image.use {
                val bitmap = checkNotNull(Bitmap.wrapHardwareBuffer(checkNotNull(it.hardwareBuffer), null))
                    .copy(Bitmap.Config.ARGB_8888, false)
                return IntArray(WIDTH * HEIGHT).also { pixels ->
                    bitmap.getPixels(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT)
                }
            }
        } finally {
            renderer.destroy()
            reader.close()
        }
    }

    private companion object {
        const val WIDTH = 480
        const val HEIGHT = 96
    }
}
