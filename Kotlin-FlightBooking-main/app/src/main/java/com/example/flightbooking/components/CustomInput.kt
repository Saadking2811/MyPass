package com.example.flightbooking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

var inputStyle: Modifier = Modifier.fillMaxWidth()

@Composable
fun CustomInput(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    maxLines: Int = 1,
    placeHolder: String = "",
    showIcon: Boolean = true,
    icon: ImageVector = Icons.Default.Email,
    iconOnClick: () -> Unit = {},
    textColor: Color = Color.White,
    inputType: VisualTransformation = VisualTransformation.None

    ) {

    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        label = {Text(text=label)},
        maxLines = maxLines,
        placeholder = {
            Text(
                text = placeHolder,
                style = MaterialTheme.typography.labelLarge
                    .copy(
                        color = Color.White
                    )
            )
        },
        leadingIcon = {
            if (showIcon) {

                Icon(
                    imageVector = icon, contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.clickable(onClick = iconOnClick)
                )
            }
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = textColor,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = textColor,
            unfocusedPlaceholderColor = textColor,
            unfocusedLabelColor = textColor

        ),
visualTransformation = inputType

        )

}

