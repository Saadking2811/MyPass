package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.EmeraldGreen

/** MyPass brand mark — emerald rounded square with ascending airplane silhouette. */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 9,
    showText: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius.dp))
                .background(EmeraldGreen),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x22FFFFFF), Color.Transparent),
                        center = Offset(size.width * 0.28f, size.height * 0.22f),
                        radius = size.minDimension * 0.55f
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.28f, size.height * 0.22f)
                )

                withTransform({
                    translate(cx, cy)
                    rotate(degrees = -30f, pivot = Offset.Zero)
                }) {
                    val s = size.minDimension * 0.017f
                    val white = Color.White
                    val fuselage = androidx.compose.ui.graphics.Path().apply {
                        moveTo(-3f * s, -22f * s); lineTo(4f * s, -22f * s)
                        lineTo(5f * s, -12f * s); lineTo(5f * s, 12f * s)
                        lineTo(8f * s, 18f * s);  lineTo(8f * s, 22f * s)
                        lineTo(2f * s, 21f * s);  lineTo(-2f * s, 21f * s)
                        lineTo(-8f * s, 22f * s); lineTo(-8f * s, 18f * s)
                        lineTo(-5f * s, 12f * s); lineTo(-5f * s, -12f * s); close()
                    }
                    drawPath(fuselage, white)
                    val wings = androidx.compose.ui.graphics.Path().apply {
                        moveTo(-5f * s, -3f * s); lineTo(-26f * s, 8f * s)
                        lineTo(-26f * s, 12f * s); lineTo(-5f * s, 5f * s)
                        lineTo(5f * s, 5f * s);    lineTo(26f * s, 12f * s)
                        lineTo(26f * s, 8f * s);   lineTo(5f * s, -3f * s); close()
                    }
                    drawPath(wings, white)
                    val tail = androidx.compose.ui.graphics.Path().apply {
                        moveTo(-10f * s, 15f * s); lineTo(-3f * s, 12f * s)
                        lineTo(3f * s, 12f * s);   lineTo(10f * s, 15f * s)
                        lineTo(10f * s, 18f * s);  lineTo(-10f * s, 18f * s); close()
                    }
                    drawPath(tail, white)
                }
            }
        }
        if (showText) {
            Text(
                "MyPass",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp
                ),
                color = EmeraldGreen
            )
        }
    }
}
