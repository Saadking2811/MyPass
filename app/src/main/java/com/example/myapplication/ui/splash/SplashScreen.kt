package com.example.myapplication.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.BrandLogo
import com.example.myapplication.ui.theme.EmeraldGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * App splash — drifting clouds, gliding airplane along a Bezier arc, then a brand reveal.
 * Plays a brief takeoff haptic mid-glide. Calls [onFinished] once the reveal animation completes.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current

    val cloudShift   = remember { Animatable(0f) }
    val planeProg    = remember { Animatable(0f) }
    val planeFade    = remember { Animatable(1f) }
    val logoAlpha    = remember { Animatable(0f) }
    val logoScale    = remember { Animatable(0.92f) }
    val wordAlpha    = remember { Animatable(0f) }
    val wordY        = remember { Animatable(8f) }
    val ruleProg     = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { cloudShift.animateTo(1f, tween(3700, easing = LinearEasing)) }

        launch {
            delay(250)
            planeProg.animateTo(1f, tween(2200, easing = FastOutSlowInEasing))
            planeFade.animateTo(0f, tween(280))
        }
        launch {
            delay(900)
            triggerTakeoffHaptic(context)
        }

        launch {
            delay(1700)
            launch { logoAlpha.animateTo(1f, tween(450)) }
            launch { logoScale.animateTo(1f, tween(550, easing = FastOutSlowInEasing)) }
        }
        launch {
            delay(1900)
            launch { wordAlpha.animateTo(1f, tween(500)) }
            launch { wordY.animateTo(0f, tween(550, easing = FastOutSlowInEasing)) }
        }
        launch { ruleProg.animateTo(1f, tween(500, delayMillis = 2300, easing = FastOutSlowInEasing)) }
        launch { taglineAlpha.animateTo(1f, tween(450, delayMillis = 2500)) }

        delay(3400)
        onFinished()
    }

    data class Cloud(val x: Float, val y: Float, val w: Float, val opacity: Float, val depth: Float)
    val clouds = remember {
        val rng = kotlin.random.Random(31)
        List(7) {
            Cloud(
                x = rng.nextFloat() * 1.3f - 0.15f,
                y = rng.nextFloat() * 0.35f + 0.08f,
                w = rng.nextFloat() * 90f + 70f,
                opacity = 0.04f + rng.nextFloat() * 0.05f,
                depth = rng.nextFloat()
            )
        }
    }

    val cloudBaseColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            clouds.forEach { c ->
                val parallaxShift = cloudShift.value * (-30f - 30f * c.depth)
                val cx = (c.x * size.width + parallaxShift)
                val cy = c.y * size.height
                drawCloud(cx, cy, c.w.dp.toPx(), cloudBaseColor.copy(alpha = c.opacity), this)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = -size.width * 0.10f
            val startY = size.height * 0.48f
            val ctrlX  = size.width * 0.50f
            val ctrlY  = size.height * 0.30f
            val endX   = size.width * 1.10f
            val endY   = size.height * 0.22f

            fun point(t: Float): Offset {
                val omt = 1f - t
                return Offset(
                    omt * omt * startX + 2 * omt * t * ctrlX + t * t * endX,
                    omt * omt * startY + 2 * omt * t * ctrlY + t * t * endY
                )
            }
            fun tangent(t: Float): Float {
                val dx = 2 * (1 - t) * (ctrlX - startX) + 2 * t * (endX - ctrlX)
                val dy = 2 * (1 - t) * (ctrlY - startY) + 2 * t * (endY - ctrlY)
                return kotlin.math.atan2(dy, dx)
            }

            val prog = planeProg.value
            val fade = planeFade.value

            if (prog > 0f && fade > 0f) {
                val tailLen = 50
                for (i in 1..tailLen) {
                    val tBack = (prog - i * 0.012f).coerceAtLeast(0f)
                    if (tBack <= 0f) break
                    val tNext = (tBack - 0.012f).coerceAtLeast(0f)
                    val p1 = point(tBack)
                    val p2 = point(tNext)
                    val ratio = 1f - i / tailLen.toFloat()
                    val alpha = ratio * ratio * 0.55f * fade
                    val width = (3.5f * ratio + 0.4f).dp.toPx()
                    drawLine(
                        color = EmeraldGreen.copy(alpha = alpha),
                        start = p1, end = p2,
                        strokeWidth = width, cap = StrokeCap.Round
                    )
                }

                val head = point(prog)
                val angleDeg = tangent(prog) * 57.2958f
                withTransform({
                    translate(head.x, head.y)
                    rotate(degrees = angleDeg, pivot = Offset.Zero)
                }) {
                    drawAirplaneSideRealistic(fade)
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            BrandLogo(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value; scaleY = logoScale.value
                    },
                cornerRadius = 18
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "MyPass",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-1.4).sp
                ),
                color = EmeraldGreen,
                modifier = Modifier.graphicsLayer {
                    alpha = wordAlpha.value
                    translationY = wordY.value
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(modifier = Modifier.width(72.dp).height(1.dp)) {
                val w = size.width * ruleProg.value
                drawLine(
                    color = EmeraldGreen.copy(alpha = 0.6f),
                    start = Offset((size.width - w) / 2f, size.height / 2),
                    end   = Offset((size.width + w) / 2f, size.height / 2),
                    strokeWidth = 1.2f, cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "EVERY JOURNEY MATTERS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
            )
        }
    }
}

/** Soft, pale cloud blob — purely decorative for the splash. */
private fun drawCloud(cx: Float, cy: Float, w: Float, color: Color, scope: DrawScope) {
    with(scope) {
        drawCircle(color, w * 0.28f, Offset(cx, cy))
        drawCircle(color, w * 0.22f, Offset(cx + w * 0.22f, cy + w * 0.04f))
        drawCircle(color, w * 0.18f, Offset(cx - w * 0.20f, cy + w * 0.06f))
        drawCircle(color, w * 0.16f, Offset(cx + w * 0.40f, cy + w * 0.09f))
        drawCircle(color, w * 0.14f, Offset(cx - w * 0.36f, cy + w * 0.10f))
    }
}

/**
 * Side-view airplane silhouette in emerald — drawn around (0,0), nose pointing +X.
 * Use rotate() before calling to align with flight direction.
 */
private fun DrawScope.drawAirplaneSideRealistic(alpha: Float) {
    val color = EmeraldGreen.copy(alpha = alpha)
    val accent = Color(0xFFCFE7D7).copy(alpha = alpha * 0.85f)
    val s = 1.3f

    val fuselage = androidx.compose.ui.graphics.Path().apply {
        moveTo(46f * s, 0f)
        quadraticBezierTo(55f * s, -3f * s, 46f * s, -6f * s)
        lineTo(-32f * s, -6f * s)
        lineTo(-48f * s, -3f * s)
        lineTo(-48f * s, 3f * s)
        lineTo(-32f * s, 6f * s)
        lineTo(46f * s, 6f * s)
        quadraticBezierTo(55f * s, 3f * s, 46f * s, 0f)
        close()
    }
    drawPath(fuselage, color)

    val wing = androidx.compose.ui.graphics.Path().apply {
        moveTo(8f * s, 3f * s)
        lineTo(-18f * s, 22f * s)
        lineTo(-6f * s, 23f * s)
        lineTo(22f * s, 5f * s)
        close()
    }
    drawPath(wing, color)

    val vFin = androidx.compose.ui.graphics.Path().apply {
        moveTo(-36f * s, -6f * s)
        lineTo(-44f * s, -22f * s)
        lineTo(-48f * s, -6f * s)
        close()
    }
    drawPath(vFin, color)

    val hStab = androidx.compose.ui.graphics.Path().apply {
        moveTo(-38f * s, -2f * s)
        lineTo(-50f * s, -8f * s)
        lineTo(-42f * s, -2f * s)
        close()
    }
    drawPath(hStab, color)

    drawRoundRect(color,
        topLeft = Offset(-4f * s, 12f * s),
        size = Size(20f * s, 6f * s),
        cornerRadius = CornerRadius(3f * s)
    )

    drawCircle(accent, 2.6f * s, Offset(40f * s, -2f * s))

    for (i in 0..6) {
        val wx = 32f * s - i * 9f * s
        drawCircle(accent, 1.4f * s, Offset(wx, -1f * s))
    }
}

/** Brief takeoff haptic — used at the moment the plane begins to climb. */
private fun triggerTakeoffHaptic(context: android.content.Context) {
    runCatching {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vm = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE)
                as android.os.VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val effect = android.os.VibrationEffect.createWaveform(
                longArrayOf(0, 40, 40, 80),
                intArrayOf(0, 90, 0, 140),
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(110L)
        }
    }
}
