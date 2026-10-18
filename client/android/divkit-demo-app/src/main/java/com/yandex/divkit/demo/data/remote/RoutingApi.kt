package com.yandex.divkit.demo.data.remote

import com.yandex.divkit.demo.data.entities.RoutBody
import com.yandex.divkit.demo.data.entities.RoutResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RoutingApi {
    @Headers("Content-Type: application/json")
    @POST("routing/")
    suspend fun routing(
        @Body routingBody: RoutBody
    ): RoutResponse
}

