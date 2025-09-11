package com.example.bubtrack.utill

sealed class Resource<T>(val data: T? = null, val msg: String? = null) {
    class Idle<T> : Resource<T>()
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(msg: String? = null, data: T? = null) : Resource<T>(data, msg)
    class Success<T>(data: T?) : Resource<T>(data)
}