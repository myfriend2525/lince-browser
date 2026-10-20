/*
 * The AGSL shader below is a modified port of the "Liquid Metal" fragment shader from
 * Paper Shaders (https://github.com/paper-design/shaders), Copyright Paper Design,
 * licensed under the Apache License, Version 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * Changes by Lince Browser: ported from WebGL GLSL to Android AGSL; kept only the
 * full-canvas (no shape, no image) path; replaced screen-space derivatives with a fixed
 * antialiasing width; fixed the button's parameters as constants; flipped Y for Android
 * coordinates and removed the colour-banding dither and tint pass (white tint is identity).
 *
 * The 2D simplex noise is by Ian McEwan and Ashima Arts (MIT License).
 */
package com.lince.browser.shared.ui

import android.graphics.RuntimeShader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.nativeCanvas

actual fun createLiquidMetalBrushFactory(): LiquidMetalBrushFactory? =
    runCatching { AndroidLiquidMetalBrushFactory(RuntimeShader(LIQUID_METAL_AGSL)) }.getOrNull()

private class AndroidLiquidMetalBrushFactory(
    private val shader: RuntimeShader,
) : LiquidMetalBrushFactory {
    // Software canvases (for example view-to-bitmap captures) throw for RuntimeShader paints.
    override fun supports(canvas: Canvas): Boolean = canvas.nativeCanvas.isHardwareAccelerated

    override fun brush(timeSeconds: Float, darkSurface: Boolean): Brush {
        shader.setFloatUniform("iTime", timeSeconds)
        // Paper's LiquidMetalBorder defaults: #aaaaac on dark surfaces, #888888 on light ones.
        if (darkSurface) {
            shader.setFloatUniform("colorBack", 0xAA / 255f, 0xAA / 255f, 0xAC / 255f, 1f)
        } else {
            shader.setFloatUniform("colorBack", 0x88 / 255f, 0x88 / 255f, 0x88 / 255f, 1f)
        }
        return object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                shader.setFloatUniform("iResolution", size.width, size.height)
                return shader
            }
        }
    }
}

private const val LIQUID_METAL_AGSL = """
uniform float2 iResolution;
uniform float iTime;
uniform float4 colorBack;

const float PI = 3.14159265;
const float REPETITION = 7.0;
const float SOFTNESS = 0.05;
const float SHIFT_RED = 0.6;
const float SHIFT_BLUE = 0.3;
const float DISTORTION = 0.1;
const float ANGLE = 90.0;
const float SCALE = 3.0;
const float AA = 0.02;

float2 rotate(float2 v, float a) {
  float s = sin(a);
  float c = cos(a);
  return float2(c * v.x - s * v.y, s * v.x + c * v.y);
}

float3 mod289(float3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
float2 mod289(float2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
float3 permute(float3 x) { return mod289(((x * 34.0) + 1.0) * x); }

float snoise(float2 v) {
  const float4 C = float4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);
  float2 i = floor(v + dot(v, C.yy));
  float2 x0 = v - i + dot(i, C.xx);
  float2 i1 = (x0.x > x0.y) ? float2(1.0, 0.0) : float2(0.0, 1.0);
  float4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;
  i = mod289(i);
  float3 p = permute(permute(i.y + float3(0.0, i1.y, 1.0)) + i.x + float3(0.0, i1.x, 1.0));
  float3 m = max(0.5 - float3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
  m = m * m;
  m = m * m;
  float3 x = 2.0 * fract(p * C.www) - 1.0;
  float3 h = abs(x) - 0.5;
  float3 ox = floor(x + 0.5);
  float3 a0 = x - ox;
  m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
  float3 g;
  g.x = a0.x * x0.x + h.x * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

float getColorChanges(float c1, float c2, float stripeP, float3 w, float blur, float bump) {
  float ch = mix(c2, c1, smoothstep(0.0, 2.0 * blur, stripeP));
  float border = w.x;
  ch = mix(ch, c2, smoothstep(border, border + 2.0 * blur, stripeP));
  border = w.x + 0.4 * (1.0 - bump) * w.y;
  ch = mix(ch, c1, smoothstep(border, border + 2.0 * blur, stripeP));
  border = w.x + 0.5 * (1.0 - bump) * w.y;
  ch = mix(ch, c2, smoothstep(border, border + 2.0 * blur, stripeP));
  border = w.x + w.y;
  ch = mix(ch, c1, smoothstep(border, border + 2.0 * blur, stripeP));
  float gradientT = (stripeP - w.x - w.y) / w.z;
  float gradient = mix(c1, c2, smoothstep(0.0, 1.0, gradientT));
  return mix(ch, gradient, smoothstep(border, border + 0.5 * blur, stripeP));
}

half4 main(float2 fragCoord) {
  float t = 0.3 * (iTime + 2.8);

  float2 borderUV = fragCoord / iResolution;
  float2 mask = min(borderUV, 1.0 - borderUV);
  float2 pixelThickness = min(250.0 / iResolution, float2(0.5));
  float maskX = pow(smoothstep(0.0, pixelThickness.x, mask.x), 0.25);
  float maskY = pow(smoothstep(0.0, pixelThickness.y, mask.y), 0.25);
  float edge = clamp(1.0 - maskX * maskY, 0.0, 1.0);

  // Responsive, aspect-preserving UV zoomed by SCALE; Android Y already points down.
  float2 uv = (fragCoord - 0.5 * iResolution) / max(iResolution.x, iResolution.y);
  uv = uv / SCALE + 0.5;
  float cycleWidth = REPETITION * 2.0;

  float2 rotatedUV = uv - float2(0.5);
  float angle = (-ANGLE + 70.0) * PI / 180.0;
  rotatedUV = rotate(rotatedUV, angle) + float2(0.5);

  float opacity = 1.0 - smoothstep(0.9 - 2.0 * AA, 0.9, edge);

  float diagBLtoTR = rotatedUV.x - rotatedUV.y;
  float diagTLtoBR = rotatedUV.x + rotatedUV.y;

  float3 color1 = float3(0.98, 0.98, 1.0);
  float3 color2 = float3(0.1, 0.1, 0.1 + 0.1 * smoothstep(0.7, 1.3, diagTLtoBR));

  float2 gradUV = uv - 0.5;
  float dist = length(gradUV + float2(0.0, 0.2 * diagBLtoTR));
  gradUV = rotate(gradUV, (0.25 - 0.2 * diagBLtoTR) * PI);
  float direction = gradUV.x;

  float bump = 1.0 - pow(1.8 * dist, 1.2);
  bump *= pow(uv.y, 0.3);

  float thinStrip1Ratio = 0.12 / cycleWidth * (1.0 - 0.4 * bump);
  float thinStrip2Ratio = 0.07 / cycleWidth * (1.0 + 0.4 * bump);
  float wideStripRatio = 1.0 - thinStrip1Ratio - thinStrip2Ratio;
  float thinStrip1Width = cycleWidth * thinStrip1Ratio;
  float thinStrip2Width = cycleWidth * thinStrip2Ratio;

  float noise = snoise(uv - t);
  edge += (1.0 - edge) * DISTORTION * noise;

  direction += diagBLtoTR;
  direction -= 2.0 * noise * diagBLtoTR * (smoothstep(0.0, 1.0, edge) * (1.0 - smoothstep(0.0, 1.0, edge)));

  bump *= clamp(pow(uv.y, 0.1), 0.3, 1.0);
  direction *= (0.1 + (1.1 - edge) * bump);
  direction *= (0.4 + 0.6 * (1.0 - smoothstep(0.5, 1.0, edge)));
  direction += 0.18 * (smoothstep(0.1, 0.2, uv.y) * (1.0 - smoothstep(0.2, 0.4, uv.y)));
  direction += 0.03 * (smoothstep(0.1, 0.2, 1.0 - uv.y) * (1.0 - smoothstep(0.2, 0.4, 1.0 - uv.y)));
  direction *= (0.5 + 0.5 * pow(uv.y, 2.0));
  direction *= cycleWidth;
  direction -= t;

  float colorDispersion = clamp(1.0 - bump, 0.0, 1.0);
  float dispersionRed = colorDispersion;
  dispersionRed += 0.03 * bump * noise;
  dispersionRed += 5.0 * (smoothstep(-0.1, 0.2, uv.y) * (1.0 - smoothstep(0.1, 0.5, uv.y))) *
    (smoothstep(0.4, 0.6, bump) * (1.0 - smoothstep(0.4, 1.0, bump)));
  dispersionRed -= diagBLtoTR;

  float dispersionBlue = colorDispersion * 1.3;
  dispersionBlue += (smoothstep(0.0, 0.4, uv.y) * (1.0 - smoothstep(0.1, 0.8, uv.y))) *
    (smoothstep(0.4, 0.6, bump) * (1.0 - smoothstep(0.4, 0.8, bump)));
  dispersionBlue -= 0.2 * edge;

  dispersionRed *= SHIFT_RED / 20.0;
  dispersionBlue *= SHIFT_BLUE / 20.0;

  float blur = SOFTNESS / 15.0;
  float3 w = float3(thinStrip1Width, thinStrip2Width, wideStripRatio);
  w.y -= 0.02 * smoothstep(0.0, 1.0, edge + bump);

  float stripeR = fract(direction + dispersionRed);
  float r = getColorChanges(color1.r, color2.r, stripeR, w, blur + AA, bump);
  float stripeG = fract(direction);
  float g = getColorChanges(color1.g, color2.g, stripeG, w, blur + AA, bump);
  float stripeB = fract(direction - dispersionBlue);
  float b = getColorChanges(color1.b, color2.b, stripeB, w, blur + AA, bump);

  float3 color = float3(r, g, b) * opacity;
  float3 bgColor = colorBack.rgb * colorBack.a;
  color = color + bgColor * (1.0 - opacity);
  opacity = opacity + colorBack.a * (1.0 - opacity);
  return half4(half3(color), half(opacity));
}
"""
