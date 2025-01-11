package com.yandex.divkit.demo.data.entities.res

/**
 * Created by vahid on 05/09/2015.
 */
class Response<T>(//    @SerializedName("resultCode")
    var resultCode: Int, //    @SerializedName("resultCode")
    var resultMessage: String, var response: T, var responseList: List<T>
)