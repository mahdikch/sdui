package com.yandex.divkit.demo.data.entities

data class RoutResponse(
    val shape: String,
    val SubRoutes: List<SubRoute>
)

data class SubRoute(
    val Name: String
)

