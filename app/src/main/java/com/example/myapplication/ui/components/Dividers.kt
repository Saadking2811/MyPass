package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.EmeraldGreen

/** Single emerald accent bar — used next to titles in the top bar. */
@Composable
fun GoldAccent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(3.dp).height(20.dp)
            .clip(RoundedCornerShape(1.5.dp))
            .background(EmeraldGreen)
    )
}

/** Hairline divider — themed against `outlineVariant`. */
@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/** Indented hairline used inside grouped menu cards. */
@Composable
fun Divider2() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}
