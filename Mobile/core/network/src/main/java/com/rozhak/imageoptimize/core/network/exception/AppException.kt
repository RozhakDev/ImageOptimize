package com.rozhak.imageoptimize.core.network.exception

import java.io.IOException

sealed class AppException(override val message: String) : IOException(message) {
    class ClientError(message: String) : AppException(message)
    class ValidationError(message: String) : AppException(message)
    class ServerError(message: String) : AppException(message)
    class NetworkError(message: String = "ERR_NETWORK: Gagal terhubung ke Gateway. Periksa koneksi Anda.") : AppException(message)
    class UnknownError(message: String = "ERR_UNKNOWN: Terjadi kesalahan yang tidak terdefinisi.") : AppException(message)
}
