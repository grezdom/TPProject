package com.example.httpsrequest.data

data class TransactionItem(
    val type : String,
    val volume : Long,
    val cbAccessKey : String,
    val cbSecret : String,
    val topSpread : Long,
    val targetPrice : Long,
    val bottomSpread : Long,
    val asset : String,
    val walletAddress : String,
    val validationTokenId : Long,
    val tokenSignature : String,
    val releasedBy : String
):java.io.Serializable
