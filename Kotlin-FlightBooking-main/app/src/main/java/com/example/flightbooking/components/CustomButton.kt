package com.example.flightbooking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun CustomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    color: Color = PrimaryColor,
    outlineColor: Color = Color.Transparent,
    outlineTextColor: Color = PrimaryColor,
    outlineBrColor: Color = PrimaryColor,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    outline: Boolean = false
){
    val colorBtn = if (outline) outlineColor  else color
    val colorBtnTxt = if (outline)  outlineTextColor else  textColor
    Row(
        modifier = modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorBtn,


            ),
            border = if(outline)  BorderStroke(width = 2.dp, color = outlineBrColor) else null,
            modifier = modifier.fillMaxWidth()
                .height(50.dp)
            ,
            shape= RoundedCornerShape(10.dp),
            enabled = enabled
        ) {
            Text(text=text, style=MaterialTheme.typography.labelLarge
                .copy(color = colorBtnTxt,
                    ))
        }
    }

}