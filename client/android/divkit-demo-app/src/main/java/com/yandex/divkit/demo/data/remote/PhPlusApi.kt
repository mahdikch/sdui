package com.yandex.divkit.demo.data.remote


import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.entities.res.Response
import com.yandex.divkit.demo.data.entities.res.ResponsePhPlus
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface   PhPlusApi {

    @Headers("Content-Type: application/json")
    @POST("CallService/coordinator")
//    @POST("phplus/1/coordinator")
    suspend fun phPlus(
        @Body request: MutableMap<String,String>,
    ): Response<MutableMap<String,String>>

    @Headers("Content-Type: application/json")
    @GET("users/rest/login")
    suspend fun getList(
        @Body request: String,
    ): Response<ListItemDto>

}