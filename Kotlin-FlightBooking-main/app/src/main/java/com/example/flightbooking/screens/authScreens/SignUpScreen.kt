package com.example.flightbooking.screens.authScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flightbooking.components.CustomButton
import com.example.flightbooking.components.CustomInput
import com.example.flightbooking.navigation.FlightScreens
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun SignUpScreen(navController: NavController,
                 viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                 ) {
    val state = rememberScrollState()
    LaunchedEffect(Unit) { state.animateScrollTo(100) }
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var name by remember {
        mutableStateOf("")
    }

    var phoneNumber by remember {
        mutableStateOf("")
    }


    var showPassword by remember {
        mutableStateOf(false)
    }

    var contest = LocalContext.current

    val isLoading = viewModel.isLoading

    val loginErr = viewModel.loginErr

    val keyboardController = LocalSoftwareKeyboardController.current
    fun isValidInput (): Boolean {
        return (email.trim().isNotEmpty() && name.trim().isNotEmpty() && phoneNumber.trim().isNotEmpty()
                && password.trim().isNotEmpty())
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
//            .background(color = Color.White)
//            .paint(
//                painterResource(id = R.drawable.sign_up),
//                contentScale = ContentScale.FillWidth
//            )
    ) {

        Column(
            modifier = Modifier
                .weight(3F)
                .verticalScroll(state)
                .padding(bottom = 10.dp)
                .background(
                    color = PrimaryColor,
                    shape =
                    RoundedCornerShape(bottomStartPercent = 13, bottomEndPercent = 13)
                )
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.headlineMedium
                    .copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ), modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp)
            )

            CustomInput(
                text = name,
                onTextChange = { name = it },
                label = "Name",
//                placeHolder = "Name"
            )

            CustomInput(
                text = email,
                onTextChange = { email = it },
                label = "Email",
//                placeHolder = "Email"
            )


            CustomInput(
                text = phoneNumber,
                onTextChange = {phoneNumber = it },
                label = "Phone number",
//                placeHolder = "Phone Number"
            )


            CustomInput(
                text = password,
                onTextChange = { password = it },
                label = "Password",
                iconOnClick = { showPassword = !showPassword },
                inputType = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//                placeHolder = "Password"
                        icon = Icons.Filled.RemoveRedEye
            )

            CustomInput(
                text = confirmPassword,
                onTextChange = { confirmPassword = it },
                label = "Confirm Password",
//                placeHolder = "Confirm Password",
                icon = Icons.Filled.RemoveRedEye,
                iconOnClick = { showPassword = !showPassword },
                inputType = if(showPassword) VisualTransformation.None else PasswordVisualTransformation()

            )

            Box(modifier = Modifier.padding(10.dp))
        }
        Column(
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxWidth()
                .weight(1F)
                .padding(vertical = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            if(isLoading) {
                LinearProgressIndicator()
            }else {
            CustomButton(onClick = {
                                   /*TODO*/
                                keyboardController?.hide()

                Log.d("test", "${email}, ${password},${confirmPassword}," +
                        phoneNumber)
if(email.isEmpty() or password.isEmpty() or name.isEmpty() or phoneNumber.isEmpty()
    or confirmPassword.isEmpty()) {
    Toast.makeText(
        contest,
        "All field must be filled",
        Toast.LENGTH_SHORT
    ).show()
}
                else {
    viewModel.createUserWithEmailAndPassword(email, password, name, phoneNumber) {

        navController.navigate(FlightScreens.HomeScreen.name)
    }
}
                                   },
                text = "Create Account",
                outline = true
            )}
            Text(text="Already have an account ? Sign in",
                style=MaterialTheme.typography.labelMedium.copy(
                    color = Color.Gray,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.clickable(
                    onClick = {navController.navigate(FlightScreens.LoginScreen.name)}
                )
                )

            if (loginErr.isNotEmpty()) {
                Toast.makeText(contest, loginErr, Toast.LENGTH_SHORT).show()
            }

        }
    }

}
