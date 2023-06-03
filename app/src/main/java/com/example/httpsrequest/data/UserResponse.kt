package com.example.httpsrequest.data

data class UserResponse(
    val nick : String,
    val authenticationPublicKey : String,
    val validationPublicKey : String,
    val status : String
) : java.io.Serializable
