package com.yandex.divkit.demo.data.remote


import com.yandex.divkit.demo.data.entities.ListItemDto
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.entities.res.Response
import com.yandex.divkit.demo.data.entities.res.ResponsePhPlus
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PhPlusApi {

    @Headers("Content-Type: application/json")
    @POST("CallService/coordinator") //local
//    @POST("phplusLocal/1/coordinator
//
//
//    ")
//    @POST("phplus/1/coordinator")//main
    suspend fun phPlus(
        @Header("user-id") phId: String,
        @Body request: Map<String, String>,
    ): Response<Map<String, String>>

    @Multipart
    @POST("CallService/coordinator")
    suspend fun phPlusWithFiles(
        @Header("user-id") phId: String,
        @Part("requestScreen") requestScreen: RequestBody,
        @Part files: List<@JvmSuppressWildcards MultipartBody.Part>,
    ): Response<Map<String, String>>

    @Headers("Content-Type: application/json")
    @GET("users/rest/login")
    suspend fun getList(
        @Body request: String,
    ): Response<ListItemDto>

}
