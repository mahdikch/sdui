package com.yandex.divkit.demo.data.entities.res

import com.google.gson.annotations.SerializedName

/**
 * Coordinator API envelope. Field names kept via SerializedName for R8 release builds.
 */
class Response<T>(
    @SerializedName("resultCode")
    var resultCode: Int = 0,
    @SerializedName("resultMessage")
    var resultMessage: String = "",
    @SerializedName("response")
    var response: T? = null,
    @SerializedName("responseList")
    var responseList: List<T>? = null,
)