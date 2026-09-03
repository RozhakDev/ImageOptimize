package com.rozhak.imageoptimize.core.model

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: String = "ERR_UNKNOWN") : Result<Nothing>()
    object Loading : Result<Nothing>()
}
