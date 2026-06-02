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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cinematic splash — pure emerald + white. Inspired by premium airline splash
 * sequences (Singapore Airlines, Emirates): a thin horizontal "runway" line
 * opens the scene, concentric pulses radiate from the brand mark, the logo
 * scales in with a soft overshoot, the wordmark slides up, a hairline
 * underscore draws across, then a tagline reveals. No gold anywhere.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current

    val bgAlpha       = remember { Animatable(0f) }
    val runwayLine    = remember { Animatable(0f) }   // 0..1 horizontal line draw
    val runwayFade    = remember { Animatable(1f) }
    val ring1Progress = remember { Animatable(0f) }
    val ring2Progress = remember { Animatable(0f) }
    val ring3Progress = remember { Animatable(0f) }

    val logoAlpha     = remember { Animatable(0f) }
    val logoScale     = remember { Animatable(0.55f) }

    val wordAlpha     = remember { Animatable(0f) }
    val wordY         = remember { Animatable(20f) }
    val underline     = remember { Animatable(0f) }
    val taglineAlpha  = remember { Animatable(0f) }

    val planeProg     = remember { Animatable(0f) }
    val planeFade     = remember { Animatable(1f) }

    // Soft breathing pulse on the logo (subtle)
    val breath by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0.99f,
        targetValue  = 1.03f,
        animationSpec = infiniteRepeatable(animation = tween(2400, easing = FastOutSlowInEasing)),
        label = "breath"
    )

    LaunchedEffect(Unit) {
        // 1. Background fade in
        launch { bgAlpha.animateTo(1f, tween(180, easing = LinearEasing)) }

        // 2. Runway line draws from center outward, then fades
        launch {
            delay(120)
            runwayLine.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
            delay(220)
            runwayFade.animateTo(0f, tween(900))
        }

        // 3. Concentric pulses radiate, staggered
        launch { delay(420);  ring1Progress.animateTo(1f, tween(1100, easing = LinearOutSlowInEasing)) }
        launch { delay(700);  ring2Progress.animateTo(1f, tween(1300, easing = LinearOutSlowInEasing)) }
        launch { delay(980);  ring3Progress.animateTo(1f, tween(1500, easing = LinearOutSlowInEasing)) }

        // 4. Logo scale-in with overshoot
        launch {
            delay(520)
            triggerTakeoffHaptic(context)
            launch { logoAlpha.animateTo(1f, tween(420)) }
            launch { logoScale.animateTo(1f, tween(800, easing = EaseOutBack)) }
        }

        // 5. Wordmark slide-up
        launch {
            delay(1100)
            launch { wordAlpha.animateTo(1f, tween(500)) }
            launch { wordY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        }

        // 6. Hairline underscore draws under the wordmark
        launch { delay(1400); underline.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }

        // 7. Tagline fade in
        launch { delay(1700); taglineAlpha.animateTo(1f, tween(500)) }

        // 8. Plane ribbon arcs across the top
        launch {
            delay(1900)
            planeProg.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
            planeFade.animateTo(0f, tween(280))
        }

        delay(3800)
        onFinished()
    }

    // Subtle white particles
    data class Particle(val x: Float, val y: Float, val r: Float, val alpha: Float, val depth: Float)
    val particles = remember {
        val rng = kotlin.random.Random(11)
        List(30) {
            Particle(
                x = rng.nextFloat(),
                y = rng.nextFloat(),
                r = rng.nextFloat() * 1.6f + 0.5f,
                alpha = 0.08f + rng.nextFloat() * 0.15f,
                depth = rng.nextFloat()
            )
        }
    }
    // Slow drift index that recomputes each frame via ring1Progress
    val particleDrift by rememberInfiniteTransition(label = "drift").animateFloat(
        initialValue = 0f, targetValue = 200f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing)),
        label = "drift"
    )

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
                        Color(0xFF000A07)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Layer A — drifting white dust particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                drawCircle(
                    color = PureWhite.copy(alpha = p.alpha),
                    radius = p.r,
                    center = Offset(
                        x = ((p.x * size.width + particleDrift * (0.4f + p.depth)) % size.width),
                        y = p.y * size.height
                    )
                )
            }
        }

        // Layer B — runway line drawing across the center, then fading
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cy = size.height / 2
            val len = size.width * runwayLine.value
            drawLine(
                color = PureWhite.copy(alpha = runwayFade.value * 0.85f),
                start = Offset((size.width - len) / 2, cy),
                end   = Offset((size.width + len) / 2, cy),
                strokeWidth = 1.2f,
                cap = StrokeCap.Round
            )
        }

        // Layer C — three white concentric pulses from center
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val maxR = size.minDimension * 0.50f

            fun pulse(progress: Float) {
                if (progress <= 0f) return
                val r = maxR * progress
                val alpha = (1f - progress) * 0.32f
                drawCircle(
                    color = PureWhite.copy(alpha = alpha),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2f)
                )
            }
            pulse(ring1Progress.value)
            pulse(ring2Progress.value)
            pulse(ring3Progress.value)
        }

        // Layer D — plane silhouette arcing along a subtle top curve
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = -size.width * 0.10f
            val startY = size.height * 0.20f
            val ctrlX  = size.width * 0.50f
            val ctrlY  = size.height * 0.05f
            val endX   = size.width * 1.10f
            val endY   = size.height * 0.14f

            fun point(t: Float): Offset {
                val omt = 1f - t
                return Offset(
                    omt * omt * startX + 2 * omt * t * ctrlX + t * t * endX,
                    omt * omt * startY + 2 * omt * t * ctrlY + t * t * endY
                )
            }

            val prog = planeProg.value
            val fade = planeFade.value
            if (prog > 0f && fade > 0f) {
                val tailLen = 60
                for (i in 1..tailLen) {
                    val tBack = (prog - i * 0.010f).coerceAtLeast(0f)
                    if (tBack <= 0f) break
                    val tNext = (tBack - 0.010f).coerceAtLeast(0f)
                    val p1 = point(tBack)
                    val p2 = point(tNext)
                    val ratio = 1f - i / tailLen.toFloat()
                    val a = ratio * ratio * 0.55f * fade
                    val w = (2.6f * ratio + 0.3f).dp.toPx()
                    drawLine(
                        color = PureWhite.copy(alpha = a),
                        start = p1, end = p2,
                        strokeWidth = w, cap = StrokeCap.Round
                    )
                }
                val head = point(prog)
                drawCircle(PureWhite.copy(alpha = fade), 5f, head)
            }
        }

        // Layer E — centered brand reveal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_green),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value * breath
                        scaleY = logoScale.value * breath
                    }
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            // Hairline white underscore drawn from center
            Canvas(modifier = Modifier.width(140.dp).height(2.dp)) {
                val w = size.width * underline.value
                drawLine(
                    color = PureWhite.copy(alpha = 0.85f),
                    start = Offset((size.width - w) / 2f, size.height / 2),
                    end   = Offset((size.width + w) / 2f, size.height / 2),
                    strokeWidth = 1.5f, cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "EVERY JOURNEY MATTERS",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                color = PureWhite.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha.value }
            )
        }

        // Layer F — pulsing loading dots at the bottom
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadingDots(alpha = taglineAlpha.value)
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun LoadingDots(alpha: Float) {
    val t by rememberInfiniteTransition(label = "dots").animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing)),
        label = "dotsT"
    )
    Canvas(modifier = Modifier.size(width = 56.dp, height = 12.dp).graphicsLayer { this.alpha = alpha }) {
        val cy = size.height / 2
        val spacing = size.width / 4
        for (i in 0..2) {
            val phase = (t - i * 0.4f) % 1f
            val scale = if (phase < 0.5f) 0.6f + phase * 0.8f else 1.0f - (phase - 0.5f) * 0.8f
            drawCircle(
                color = PureWhite.copy(alpha = 0.55f + scale * 0.35f),
                radius = 3.2f * scale,
                center = Offset(spacing * (i + 1), cy)
            )
        }
    }
}

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
