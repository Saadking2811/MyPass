package com.example.flightbooking.model

data class Pagination(
    val count: Int,
    val limit: Int,
    val offset: Int,
    val total: Int
)