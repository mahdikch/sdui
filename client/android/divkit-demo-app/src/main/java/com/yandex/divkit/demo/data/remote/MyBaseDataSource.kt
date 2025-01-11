package com.yandex.divkit.demo.data.remote

import com.yandex.divkit.demo.data.entities.res.Response



abstract class MyBaseDataSource {

    protected suspend fun <T> getResult(
        /*tag: String,
        isShowLoading: Boolean,*/
        call: suspend () -> Response<T>
    ): Resource<T> {
        try {
            val response = call()
            if (response.resultCode == -20) {
                return error(" ${response.resultCode} ${response.resultMessage}")

            } else {
                val body = response.response
                if (body != null)
                    return Resource.success(body)
            }
            return error(" ${response.resultCode} ${response.resultMessage}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(message: String): Resource<T> {
        return Resource.error("Network call has failed for a following reason: $message", null)
    }

}