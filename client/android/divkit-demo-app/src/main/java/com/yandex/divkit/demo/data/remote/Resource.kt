package com.yandex.divkit.demo.data.remote

import android.annotation.SuppressLint

data class Resource<out T>(val status: Status, val data: T? , val message: String?) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        @SuppressLint("LogNotTimber")

        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        @SuppressLint("LogNotTimber")
        fun <T> error(
            message: String,
            data: T? = null
        ): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun <T> loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}