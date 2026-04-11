package com.example.flightbooking.screens.HomeScreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.flightbooking.R
import com.example.flightbooking.components.CustomButton
import com.example.flightbooking.components.FlightDate
import com.example.flightbooking.components.FlightLocation
import com.example.flightbooking.navigation.BottomNavigation
import com.example.flightbooking.navigation.FlightScreens
import com.example.flightbooking.ui.theme.PrimaryColor
import com.example.flightbooking.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("ResourceAsColor")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val state = rememberScrollState()

    var typeOfFlight by remember {
        mutableStateOf("round")
    }

    val showDialog = remember { mutableStateOf(false) }

    val showDateDialog = remember { mutableStateOf(false) }

    val showDateDialog2 = remember { mutableStateOf(false) }


    val dateState = rememberDatePickerState()
    val dateState2 = rememberDatePickerState()
    val deptDate = remember { mutableStateOf("pick date") }
    val arrivDate = remember { mutableStateOf("pick date") }






    fun dateStateToString(dates: DatePickerState): String {
        val millisToLocalDate = dates.selectedDateMillis?.let {
            DateUtils().convertMillisToLocalDate(it)
        }
        val dateToString = millisToLocalDate?.let {
            DateUtils().dateToString(millisToLocalDate)
        } ?: "Choose Date"

        Log.d("date", dateToString)
        return dateToString
    }
//
//

    LaunchedEffect(Unit) { state.animateScrollTo(100) }
//    val navController1: NavHostController = rememberNavController()
    viewModel.getFlights()

    Scaffold(
        bottomBar = { BottomNavigation(navController) },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight()
                .fillMaxWidth()
                .verticalScroll(state)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStartPercent = 9, bottomEndPercent = 9)
                )
        ) {
            Column(
                modifier = Modifier
                    .height(310.dp)
                    .fillMaxWidth()
                    .background(
                        PrimaryColor,
                        shape = RoundedCornerShape(bottomStartPercent = 9, bottomEndPercent = 9)
                    )
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)

                ) {

                    Image(
                        painter = painterResource(id = R.drawable.logo_white),
                        contentDescription = "",
                        modifier = Modifier
                            .height(60.dp),
                        contentScale = ContentScale.FillHeight
                    )
                }

                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp
                    )
                ) {
                    Text(
                        text = "Where are you going today ?",
                        style = MaterialTheme.typography.displaySmall
                            .copy(color = Color.White),
                        modifier = Modifier.padding(
                            end = 80.dp,
                            top = 20.dp,
                            bottom = 20.dp

                        )
                    )
                    Row {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .weight(1f)
                        ) {
                            CustomButton(
                                onClick = {
                                    typeOfFlight = "round"
                                    Log.d("round", typeOfFlight.toString())
                                }, text = "Round",
                                outline = typeOfFlight != "round",
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                textColor = if (typeOfFlight == "round") {
                                    PrimaryColor
                                } else Color.Transparent,
                                outlineTextColor = if (typeOfFlight !== "round") Color.White else Color.Transparent,
                                outlineBrColor = if (typeOfFlight !== "round") Color.White else Color.Transparent

                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                //                            .height(100.dp)
                                .weight(1f)

                        ) {
                            CustomButton(
                                onClick = { typeOfFlight = "one way" },
                                text = "One way",
                                outline = typeOfFlight !== "one way",
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                textColor = if (typeOfFlight == "one way") {
                                    PrimaryColor
                                } else Color.Transparent,
                                outlineTextColor = if (typeOfFlight !== "one way") Color.White else Color.Transparent,
                                outlineBrColor = if (typeOfFlight !== "one way") Color.White else Color.Transparent
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .weight(1f)
                        ) {
                            CustomButton(
                                onClick = {
                                    typeOfFlight = "multi"
                                }, text = "Multi City",
                                outline = typeOfFlight !== "multi",
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                textColor = if (typeOfFlight == "multi") {
                                    PrimaryColor
                                } else Color.Transparent,
                                outlineTextColor = Color.White,
                                outlineBrColor = Color.White
                            )

                        }


                    }

                }

            }
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "From")
                    Text(text = "To")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 20.dp)
                        .border(
                            width = 1.dp, color = PrimaryColor, shape = RoundedCornerShape(10.dp)
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlightLocation(countryAb = "LON", country = "London, United kingdom") {
                        showDialog.value = true
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.destarrow),
                        contentDescription = ""
                    )
                    FlightLocation("LOS", "Lagos, Nigeria") {
                        showDialog.value = true
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)

                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .height(120.dp)
                            .border(
                                width = 1.dp,
                                color = PrimaryColor,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable { showDateDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) { FlightDate(title = "Departure", date = deptDate.value) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .height(120.dp)
                            .border(
                                width = 1.dp,
                                color = PrimaryColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { showDateDialog2.value = true },
                        contentAlignment = Alignment.Center
                    ) { FlightDate(title = "Arrival", date = arrivDate.value) }

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Column {
                        Text(text = "Passenger")
                        Text(text = "1 person")
                    }


                    VerticalDivider(
                        thickness = 1.dp,
                        color = PrimaryColor,
                        modifier = Modifier.height(45.dp)
                    )
                    Column {
                        Text(text = "Ticket Class")
                        Text(text = "Business class")
                    }


                    VerticalDivider(
                        thickness = 1.dp,
                        color = PrimaryColor,
                        modifier = Modifier.height(45.dp)
                    )

                    Column {

                        Text(text = "Stops")
                        Text(text = "0 stop")
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    CustomButton(
                        onClick = { navController.navigate(FlightScreens.LoginScreen.name) },
                        text = "Continue"
                    )
                }
                //// here
            }


            MinimalDialog(onDismissRequest = { showDialog.value = false }, showDialog.value)

            DatePickerWithDialog(modifier = Modifier,showDateDialog, dateState) { deptDate.value = it }
            DatePickerWithDialog(modifier = Modifier,showDateDialog2,dateState2) { arrivDate.value = it }
        }


    }

}


@Composable
fun MinimalDialog(onDismissRequest: () -> Unit, showDialog: Boolean) {
    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismissRequest() },

            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(5.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
//                Text(
//                    text = "This is a minimal dialog",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .wrapContentSize(Alignment.Center)
//                        .clickable { },
//                    textAlign = TextAlign.Center,
//                )
                LazyColumn {
                    // Add 5 items
                    items(5) { index ->
                        Text(text = "Item: $index")
                    }


                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerWithDialog(
    modifier: Modifier = Modifier,
    showDateDialog: MutableState<Boolean>,
    dateState: DatePickerState,
    onChange: (newDate: String) -> Unit = {}
) {
//    val dateState = rememberDatePickerState()


    fun dateStateToString(dates: DatePickerState): String {
        val millisToLocalDate = dates.selectedDateMillis?.let {
            DateUtils().convertMillisToLocalDate(it)
        }
        val dateToString = millisToLocalDate?.let {
            DateUtils().dateToString(millisToLocalDate)
        } ?: "Choose Date"

        Log.d("date", dateToString)
        return dateToString
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDateDialog.value) {
            DatePickerDialog(
                onDismissRequest = { showDateDialog.value = false },
                confirmButton = {
                    Button(
                        onClick = {
                            onChange(dateStateToString(dateState))
                            showDateDialog.value = false

                        }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDateDialog.value = false }
                    ) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(
                    state =  dateState,
                    showModeToggle = true
                )
            }
        }
    }
}