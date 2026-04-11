package com.example.flightbooking.screens.authScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flightbooking.R
import com.example.flightbooking.components.CustomButton
import com.example.flightbooking.components.CustomInput
import com.example.flightbooking.navigation.FlightScreens
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var disableBtn by remember {
        mutableStateOf(true)
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

    val isLoading = viewModel.isLoading

    var contest = LocalContext.current
    var loginErr = viewModel.loginErr


    Column(
        modifier = Modifier
            .background(
                color = PrimaryColor
            )
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    color = Color.White, shape =
                    RoundedCornerShape(bottomStartPercent = 100, bottomEndPercent = 100)
                )


        ) {

            Image(
                painter = painterResource(id = R.drawable.logo), contentDescription = "",

                )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            CustomInput(
                text = email,
                onTextChange = { email = it },
                label = "Email",
//        placeHolder = "Email"
            )

            CustomInput(
                text = password,
                onTextChange = { password = it },
                label = "Password",
                icon = Icons.Filled.RemoveRedEye,
                iconOnClick = { showPassword = !showPassword },
                inputType = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
//        placeHolder = "Password"
            )

            if (isLoading) {
                LinearProgressIndicator()
            } else {
                CustomButton(
                    onClick = {
                        /*TODO*/

                        Log.d("login input", email + password)
                        Log.d("login loading", isLoading.toString())
                        if (email.isEmpty() or password.isEmpty()) {
                            Toast.makeText(
                                contest,
                                "password or email cannot be emoty",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {

                            viewModel.signInWithEmailAndPassword(email, password) {
                                navController.navigate(FlightScreens.HomeScreen.name)
                            }
                        }


                    },
                    text = "Login",
                    color = Color.White,
                    textColor = PrimaryColor,
//        modifier = Modifier.fillMaxWidth()

                )
            }



            Text(
                text = "Forgotten password ? click here",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White
                ),
                modifier = Modifier.padding(vertical = 20.dp)
            )

            if (loginErr.isNotEmpty()) {
                Toast.makeText(contest, loginErr, Toast.LENGTH_SHORT).show()
            }

        }

    }


}