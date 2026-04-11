package com.example.flightbooking.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flightbooking.R
import com.example.flightbooking.components.CustomButton
import com.example.flightbooking.components.CustomInput
import com.example.flightbooking.ui.theme.PrimaryColor

@Composable
fun CheckoutScreen(navController: NavController) {
    Column {
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(80.dp)
                .background(color = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "",
                    modifier = Modifier.size(40.dp),
//                 tint = PrimaryColor
                )
                Text(
                    text = "Checkout", style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1F)
                .background(
                    color = PrimaryColor, shape = RoundedCornerShape(
                        topStartPercent = 10,
                        topEndPercent = 10
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Column(
                modifier = Modifier.padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally


            ) {
                Image(
                    painter = painterResource(id = R.drawable.cards_logo), contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 45.dp, vertical = 20.dp)


                )

                Column(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .fillMaxWidth(0.8F)
                        .height(270.dp),
//                 horizontalArrangement = Arrangement.Center
                ) {
                    CardInput(label= "Card holder name")
                    CardInput(label= "Card number")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ){
                            CardInput(label= "Expiry Date")
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ){
                            CardInput(label= "CVV")
                        }


                    }
                }
        Spacer(modifier = Modifier.height(15.dp))
                Column(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding()
                        .fillMaxWidth(0.8F)
                        .height(170.dp),

                ){
                    FeeDetails(label="Ticket Price",
                        amount= "370$")
                    FeeDetails(label="Hotel Fee",
                        amount= "800$")

                    FeeDetails(label="Fare Tax ",
                        amount= "170$")

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 5.dp),
                    )

                    FeeDetails(label="Total Fare",
                        amount= "900$", textColor = PrimaryColor)


                }

                Spacer(modifier = Modifier.height(15.dp))
                CustomButton(onClick = { /*TODO*/ }, text = "Pay" ,
                    color = Color.White,
                    textColor = PrimaryColor,

                )
            }
        }
    }
}

@Composable
private fun FeeDetails(label: String, amount: String, textColor: Color = Color.Black) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium.copy(
            color = textColor
        ))
        Text(text = amount,style = MaterialTheme.typography.titleMedium.copy(
            color = textColor
        ))
    }
}

@Composable
private fun CardInput(label: String) {
   Column (
       modifier = Modifier
           .height(80.dp)
           .background(color = Color.Transparent)) {
       Text(
           text = label,
           style =
           MaterialTheme.typography.labelMedium,
           modifier = Modifier.padding(start = 15.dp, top = 25.dp)
       )
       CustomInput(
           text = "", onTextChange = {},
           label = "", showIcon = false,
           textColor = Color.Gray

       )
   }
}