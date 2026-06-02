package com.example.myapplication.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.EmeraldGreen

/** MyPass brand mark — uses the bundled PNG (emerald square + airplane glyph). */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 9,
    showText: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(
            painter = painterResource(id = R.drawable.logo_green),
            contentDescription = "MyPass logo",
            modifier = modifier
                .clip(RoundedCornerShape(cornerRadius.dp))
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
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
