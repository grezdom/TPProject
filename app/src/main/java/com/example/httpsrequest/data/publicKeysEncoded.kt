package com.example.httpsrequest.data

import java.io.Serializable

data class publicKeysEncoded (
    var authPKEncoded : ByteArray,
    var signPKEncoded : ByteArray
    ): Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as publicKeysEncoded

        if (!authPKEncoded.contentEquals(other.authPKEncoded)) return false
        if (!signPKEncoded.contentEquals(other.signPKEncoded)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authPKEncoded.contentHashCode()
        result = 31 * result + signPKEncoded.contentHashCode()
        return result
    }
}