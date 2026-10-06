package dev.jsinco.recipes.util

import org.bukkit.Color
import org.joml.Math
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

object ColorUtil {
    val NAME_TO_COLOR_MAP: Map<String, Color> = mapOf(
        "WHITE" to Color.WHITE,
        "SILVER" to Color.SILVER,
        "GRAY" to Color.GRAY,
        "BLACK" to Color.BLACK,
        "RED" to Color.RED,
        "MAROON" to Color.MAROON,
        "YELLOW" to Color.YELLOW,
        "OLIVE" to Color.OLIVE,
        "LIME" to Color.LIME,
        "GREEN" to Color.GREEN,
        "AQUA" to Color.AQUA,
        "TEAL" to Color.TEAL,
        "BLUE" to Color.BLUE,
        "NAVY" to Color.NAVY,
        "FUCHSIA" to Color.FUCHSIA,
        "PURPLE" to Color.PURPLE,
        "ORANGE" to Color.ORANGE,
        "PINK" to Color.FUCHSIA,
        "BRIGHT_GRAY" to Color.SILVER,
        "BRIGHT_RED" to Color.fromRGB(255, 0, 0),
        "DARK_RED" to Color.fromRGB(128, 0, 0),
    )

    fun parseColorString(hexOrValue: String): Color? {
        val standardized = hexOrValue.replace("&", "").replace("#", "").uppercase(Locale.getDefault())
        return NAME_TO_COLOR_MAP[standardized] ?: try {
            return Color.fromRGB(
                standardized.substring(0, 2).toInt(16),
                standardized.substring(2, 4).toInt(16),
                standardized.substring(4, 6).toInt(16)
            )
        } catch (_: Exception) {
            return null
        }
    }

    fun lerp(a: Color, b: Color, t: Float): Color {
        val aOk = toOklch(a)
        val bOk = toOklch(b)
        val vec = Vector3f(
            Math.lerp(aOk.x, bOk.x, t),  // L
            Math.lerp(aOk.y, bOk.y, t),  // C
            lerpAngle(aOk.z, bOk.z, t) // h
        )
        return fromOklch(vec)
    }
    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        var diff = b - a
        if (diff > Math.PI_f) {
            diff -= (2.0f * Math.PI_f)
        } else if (diff < -java.lang.Math.PI) {
            diff += (2.0f * Math.PI_f)
        }
        return a + t * diff
    }

    // https://bottosson.github.io/posts/oklab/

    fun toOklch(color: Color): Vector3f {
        val vec = toOklab(color)
        val L = vec.x
        val a = vec.y
        val b = vec.z
        return Vector3f(
            L,
            hypot(a, b),
            atan2(b, a)
        )
    }

    fun fromOklch(oklch: Vector3fc): Color {
        val L = oklch.x()
        val C = oklch.y()
        val h = oklch.z()
        val vec = Vector3f(
            L,
            C * cos(h),
            C * sin(h)
        )
        return fromOklab(vec)
    }

    fun toOklab(color: Color): Vector3f {
        val vec = toLinearSRGB(color)
        val r = vec.x
        val g = vec.y
        val b = vec.z

        val l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b
        val m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b
        val s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b

        val l_ = cbrt(l)
        val m_ = cbrt(m)
        val s_ = cbrt(s)

        return Vector3f(
            0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_,
            1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_,
            0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_
        )
    }

    fun fromOklab(oklab: Vector3fc): Color {
        val L = oklab.x()
        val a = oklab.y()
        val b = oklab.z()

        val l_ = L + 0.3963377774f * a + 0.2158037573f * b
        val m_ = L - 0.1055613458f * a - 0.0638541728f * b
        val s_ = L - 0.0894841775f * a - 1.2914855480f * b

        val l = l_ * l_ * l_
        val m = m_ * m_ * m_
        val s = s_ * s_ * s_

        val vec = Vector3f(
            4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s,
            -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s,
            -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s
        )
        return fromLinearSRGB(vec)
    }

    private fun toLinearSRGB(color: Color): Vector3f {
        return Vector3f(
            f_inv(color.red / 255.0f),
            f_inv(color.green / 255.0f),
            f_inv(color.blue / 255.0f)
        )
    }

    private fun fromLinearSRGB(linearSRGB: Vector3fc): Color {
        return Color.fromRGB(
            toIntScale(f(linearSRGB.x())),
            toIntScale(f(linearSRGB.y())),
            toIntScale(f(linearSRGB.z()))
        )
    }
    private fun toIntScale(f: Float): Int {
        return Math.clamp(0, 255, (255.0f * f).toInt())
    }

    // https://bottosson.github.io/posts/colorwrong/

    private fun f(c: Float): Float {
        return if (c >= 0.0031308f)
            1.055f * c.pow(1.0f / 2.4f) - 0.055f
        else
            12.92f * c
    }

    private fun f_inv(c: Float): Float {
        return if (c >= 0.04045) {
            ((c + 0.055f) / 1.055f).pow(2.4f)
        } else {
            c / 12.92f
        }
    }

}
