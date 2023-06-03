package com.example.httpsrequest.data

import java.io.Serializable

data class UserRequestRegister(
    val nick: String,
    val authenticationPublicKey : String,
    val validationPublicKey : String
) : Serializable