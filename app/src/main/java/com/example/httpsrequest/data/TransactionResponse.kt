package com.example.httpsrequest.data

data class TransactionResponse(
    val id : Long,
    val type : String,
    val volume : Long,
    val targetPrice : Long,
    val asset : String,
    val walletAddress : String,
    val validationTokenId : Long,
    val releasedBy : String,
    val validators : List<String>,
    val status : String
):java.io.Serializable
