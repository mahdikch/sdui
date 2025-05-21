package com.yandex.divkit.demo.data.entities

data class Constraint(
    val variableName: String,
    val disallowedList: List<String>,
    val allowedList: List<String>,
)
