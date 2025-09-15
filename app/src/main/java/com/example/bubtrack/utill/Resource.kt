package com.example.bubtrack.utill

sealed class Resource<out T>(val data: T? = null, val msg: String? = null) {
    object Idle : Resource<Nothing>()
    class Loading<out T>(data: T? = null) : Resource<T>(data)
    class Error<out T>(msg: String? = null, data: T? = null) : Resource<T>(data, msg)
    class Success<out T>(data: T?) : Resource<T>(data)
}