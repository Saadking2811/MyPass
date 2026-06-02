package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class BaggageDeclaration(
    val checkedBags: Int = 0,
    val carryOnBags: Int = 1,
    val oversizedBags: Int = 0,
    val totalWeight: Double = 0.0,
    val checkedBagWeight: Double = 23.0,
    val extraBagFee: Double = 0.0
)
