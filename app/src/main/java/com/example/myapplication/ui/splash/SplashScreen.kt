package com.example.myapplication.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.theme.SoftGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cinematic splash — deep emerald radial backdrop, slow drifting particles,
 * rotating dotted halo around the brand mark, expanding concentric pulses,
 * gold underline draw and a final ribbon plane that arcs across.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current

    // Background fade in
    val bgAlpha       = remember { Animatable(0f) }

    // Halo + rings around the logo
    val haloRotation  = remember { Animatable(0f) }
    val ring1Progress = remember { Animatable(0f) }
    val ring2Progress = remember { Animatable(0f) }
    val ring3Progress = remember { Animatable(0f) }

    // Brand mark
    val logoAlpha     = remember { Animatable(0f) }
    val logoScale     = remember { Animatable(0.4f) }
    val logoRotation  = remember { Animatable(-12f) }

    // Wordmark + ornament + tagline
    val wordAlpha     = remember { Animatable(0f) }
    val wordY         = remember { Animatable(16f) }
    val goldRule      = remember { Animatable(0f) }
    val taglineAlpha  = remember { Animatable(0f) }

    // Plane ribbon at the very end
    val planeProg     = remember { Animatable(0f) }
    val planeFade     = remember { Animatable(1f) }

    // Subtle breathing pulse on the logo (infinite)
    val breath by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0.98f,
        targetValue  = 1.04f,
        animationSpec = infiniteRepeatable(animation = tween(2200, easing = FastOutSlowInEasing)),
        label = "breath"
    )

    LaunchedEffect(Unit) {
        // 1. Fade up the background instantly
        launch { bgAlpha.animateTo(1f, tween(220, easing = LinearEasing)) }

        // 2. Halo spins continuously (slow), and concentric rings expand staggered
        launch { haloRotation.animateTo(360f, tween(8000, easing = LinearEasing)) }
        launch {
            delay(180)
            ring1Progress.animateTo(1f, tween(900, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(380)
            ring2Progress.animateTo(1f, tween(1100, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(620)
            ring3Progress.animateTo(1f, tween(1300, easing = LinearOutSlowInEasing))
        }

        // 3. Brand mark "pops" in
        launch {
            delay(420)
            triggerTakeoffHaptic(context)
            launch { logoAlpha.animateTo(1f, tween(380)) }
            launch { logoScale.animateTo(1f, tween(700, easing = EaseOutBack)) }
            launch { logoRotation.animateTo(0f, tween(700, easing = FastOutSlowInEasing)) }
        }

        // 4. Wordmark slide-up
        launch {
            delay(900)
            launch { wordAlpha.animateTo(1f, tween(500)) }
            launch { wordY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        }

        // 5. Gold rule draws under the wordmark
        launch {
            delay(1200)
            goldRule.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }

        // 6. Tagline fades in
        launch {
            delay(1500)
            taglineAlpha.animateTo(1f, tween(500))
        }

        // 7. Final plane arc sweep
        launch {
            delay(1700)
            planeProg.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
            planeFade.animateTo(0f, tween(300))
        }

        delay(3700)
        onFinished()
    }

    // Pre-computed dust particles
    data class Particle(val x: Float, val y: Float, val r: Float, val alpha: Float, val depth: Float)
    val particles = remember {
        val rng = kotlin.random.Random(7)
        List(35) {
            Particle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                r = rng.nextFloat() * 2.0f + 0.6f,
                alpha = 0.10f + rng.nextFloat() * 0.20f,
                depth = rng.nextFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = bgAlpha.value }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF005F3A),
                        EmeraldGreen,
                        Color(0xFF002418),
                        Color(0xFF000F09)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Layer A: drifting dust particles (slow parallax) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val drift = haloRotation.value * 0.05f * (0.5f + p.depth)
                drawCircle(
                    color = PureWhite.copy(alpha = p.alpha),
                    radius = p.r,
                    center = Offset(
                        x = (p.x * size.width + drift) % size.width,
                        y = p.y * size.height
                    )
                )
            }
        }

        // ── Layer B: 3 expanding concentric pulses, drawn from center ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val maxR = size.minDimension * 0.45f

            fun pulse(progress: Float) {
                if (progress <= 0f) return
                val r = maxR * progress
                val a = (1f - progress) * 0.35f
                drawCircle(
                    color = PureWhite.copy(alpha = a),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4f)
                )
            }
            pulse(ring1Progress.value)
            pulse(ring2Progress.value)
            pulse(ring3Progress.value)
        }

        // ── Layer C: rotating dashed halo behind the brand mark ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val r  = 76.dp.toPx()

            withTransform({
                translate(cx, cy)
                rotate(degrees = haloRotation.value, pivot = Offset.Zero)
            }) {
                // Outer dotted ring (24 dots)
                val outerR = r * 1.20f
                for (i in 0 until 24) {
                    val a = (i * 360f / 24f) * kotlin.math.PI.toFloat() / 180f
                    val dx = outerR * kotlin.math.cos(a)
                    val dy = outerR * kotlin.math.sin(a)
                    drawCircle(
                        color = SoftGold.copy(alpha = 0.7f),
                        radius = 1.8f,
                        center = Offset(dx, dy)
                    )
                }
                // Inner full ring
                drawCircle(
                    color = SoftGold.copy(alpha = 0.40f),
                    radius = r * 1.05f,
                    center = Offset.Zero,
                    style = Stroke(width = 0.8f)
                )
                // Two arc accents (top + bottom)
                drawArc(
                    color = SoftGold.copy(alpha = 0.85f),
                    startAngle = -100f,
                    sweepAngle = 30f,
                    useCenter = false,
                    topLeft = Offset(-outerR, -outerR),
                    size = androidx.compose.ui.geometry.Size(outerR * 2, outerR * 2),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = SoftGold.copy(alpha = 0.85f),
                    startAngle = 80f,
                    sweepAngle = 30f,
                    useCenter = false,
                    topLeft = Offset(-outerR, -outerR),
                    size = androidx.compose.ui.geometry.Size(outerR * 2, outerR * 2),
                    style = Stroke(width = 2.2f, cap = StrokeCap.Round)
                )
            }
        }

        // ── Layer D: plane ribbon arcing across the top half ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = -size.width * 0.10f
            val startY = size.height * 0.18f
            val ctrlX  = size.width * 0.50f
            val ctrlY  = size.height * 0.04f
            val endX   = size.width * 1.10f
            val endY   = size.height * 0.12f

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
                // Soft contrail
                val tailLen = 60
                for (i in 1..tailLen) {
                    val tBack = (prog - i * 0.010f).coerceAtLeast(0f)
                    if (tBack <= 0f) break
                    val tNext = (tBack - 0.010f).coerceAtLeast(0f)
                    val p1 = point(tBack)
                    val p2 = point(tNext)
                    val ratio = 1f - i / tailLen.toFloat()
                    val a = ratio * ratio * 0.55f * fade
                    val w = (3.0f * ratio + 0.3f).dp.toPx()
                    drawLine(
                        color = PureWhite.copy(alpha = a),
                        start = p1, end = p2,
                        strokeWidth = w, cap = StrokeCap.Round
                    )
                }

                val head = point(prog)
                val angleDeg = tangent(prog) * 57.2958f
                withTransform({
                    translate(head.x, head.y)
                    rotate(degrees = angleDeg, pivot = Offset.Zero)
                }) {
                    drawTinyAirplane(fade)
                }
            }
        }

        // ── Layer E: centered brand mark + wordmark + ornament + tagline ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Brand mark — uses the bundled PNG with breathing pulse
            Image(
                painter = painterResource(id = R.drawable.logo_green),
                contentDescription = null,
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value * breath
                        scaleY = logoScale.value * breath
                        rotationZ = logoRotation.value
                    }
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Wordmark "MyPass"
            Text(
                "MyPass",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-1.4).sp
                ),
                color = PureWhite,
                modifier = Modifier.graphicsLayer {
                    alpha = wordAlpha.value
                    translationY = wordY.value
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Gold rule drawing under the wordmark
            Canvas(modifier = Modifier.width(140.dp).height(2.dp)) {
                val w = size.width * goldRule.value
                drawLine(
                    color = SoftGold,
                    start = Offset((size.width - w) / 2f, size.height / 2),
                    end   = Offset((size.width + w) / 2f, size.height / 2),
                    strokeWidth = 1.6f, cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tagline
            Text(
                "EVERY JOURNEY MATTERS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = PureWhite.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
            )
        }

        // ── Layer F: bottom "loading" dots ──
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots(alpha = taglineAlpha.value)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/** Three pulsing dots that loop while the splash is visible. */
@Composable
private fun LoadingDots(alpha: Float) {
    val t by rememberInfiniteTransition(label = "dots").animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing)),
        label = "dotsT"
    )
    Canvas(modifier = Modifier.size(width = 60.dp, height = 14.dp).graphicsLayer { this.alpha = alpha }) {
        val cy = size.height / 2
        val spacing = size.width / 4
        for (i in 0..2) {
            val phase = (t - i * 0.4f) % 1f
            val scale = if (phase < 0.5f) 0.6f + phase * 0.8f else 1.0f - (phase - 0.5f) * 0.8f
            drawCircle(
                color = PureWhite.copy(alpha = 0.5f + scale * 0.4f),
                radius = 3.5f * scale,
                center = Offset(spacing * (i + 1), cy)
            )
        }
    }
}

/**
 * Tiny side-view airplane silhouette in white — drawn around (0,0), nose pointing +X.
 * Used by the ribbon plane that arcs across the splash near the end.
 */
private fun DrawScope.drawTinyAirplane(alpha: Float) {
    val color = PureWhite.copy(alpha = alpha)
    val s = 1.0f

    val fuselage = androidx.compose.ui.graphics.Path().apply {
        moveTo(34f * s, 0f)
        quadraticTo(40f * s, -3f * s, 34f * s, -5f * s)
        lineTo(-22f * s, -5f * s)
        lineTo(-34f * s, -3f * s)
        lineTo(-34f * s, 3f * s)
        lineTo(-22f * s, 5f * s)
        lineTo(34f * s, 5f * s)
        quadraticTo(40f * s, 3f * s, 34f * s, 0f)
        close()
    }
    drawPath(fuselage, color)

    val wing = androidx.compose.ui.graphics.Path().apply {
        moveTo(4f * s, 3f * s)
        lineTo(-14f * s, 16f * s)
        lineTo(-4f * s, 17f * s)
        lineTo(16f * s, 4f * s)
        close()
    }
    drawPath(wing, color)

    val vFin = androidx.compose.ui.graphics.Path().apply {
        moveTo(-24f * s, -5f * s)
        lineTo(-30f * s, -16f * s)
        lineTo(-34f * s, -5f * s)
        close()
    }
    drawPath(vFin, color)
}

/** Brief takeoff haptic — used at the moment the logo pops in. */
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
                longArrayOf(0, 30, 30, 60, 30, 50),
                intArrayOf(0, 100, 0, 140, 0, 90),
                -1
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120L)
        }
    }
}
