package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.EmeraldGreen
import com.example.myapplication.ui.theme.PureWhite

/** Filled emerald action button — primary call-to-action across all screens. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldGreen,
            contentColor   = PureWhite,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor   = MaterialTheme.colorScheme.outline
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = PureWhite, strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.2.sp),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Outlined emerald button — secondary action paired with [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.5.dp, EmeraldGreen),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.3.sp),
            fontWeight = FontWeight.Bold
        )
    }
}
