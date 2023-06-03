package com.example.httpsrequest.data

import java.io.Serializable


data class userRequestLogin(
    val nick : String,
    val authenticationTokenId : Long,
    val tokenSignature : String
):Serializable
