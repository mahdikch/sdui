package com.yandex.divkit.demo.data.entities

import com.yandex.div.core.DivViewFacade



data class ListItemDto(
    val id: String,
    val parentId: String,
    val titleFa: String,
    val titleEn: String,
    val constraintList: List<Constraint>,

    )
//data class ListItemDto(
//    val id: Long,
//    val title: String,
//    val senderCode: String,
//    val parentId: Long,
//    val Patch: String,
//    val view: DivViewFacade
//)