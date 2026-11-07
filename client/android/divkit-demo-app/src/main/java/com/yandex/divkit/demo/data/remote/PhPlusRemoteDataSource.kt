package com.yandex.divkit.demo.data.remote


import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class PhPlusRemoteDataSource @Inject constructor(
    private val phPlusService: PhPlusApi
) : MyBaseDataSource() {
    suspend fun phPlus(phId:String,request: HashMap<String,String>) =
        getResult { phPlusService.phPlus(phId,request) }

    suspend fun phPlusRawBody(phId: String, jsonBody: String) = getResult {
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        phPlusService.phPlusRawBody(phId, body)
    }

    suspend fun phPlusWithFiles(
        phId: String,
        requestScreenJson: String,
        files: List<MultipartBody.Part>,
    ) = getResult {
        val requestScreen = requestScreenJson.toRequestBody(
            "application/json; charset=utf-8".toMediaType()
        )
        phPlusService.phPlusWithFiles(phId, requestScreen, files)
    }

    suspend fun getList(listId: String) =
        getResult { phPlusService.getList(listId) }
}