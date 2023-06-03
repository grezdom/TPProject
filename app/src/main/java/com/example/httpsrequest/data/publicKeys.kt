package com.example.httpsrequest.data

import java.io.Serializable
import java.security.PublicKey

data class publicKeys (
    var authPublicKey : PublicKey,
    var signPublicKey: PublicKey
) : Serializable