package com.example.httpsrequest.data

data class Token(
    val id : Long,
    val userId : Long,
    val token : String,
    val expiration : String,
    val status : String
) : java.io.Serializable
