package com.example.httpsrequest.retrofitapi

import com.example.httpsrequest.data.Token
import com.example.httpsrequest.data.TransactionItem
import com.example.httpsrequest.data.TransactionResponse
import retrofit2.Call
import retrofit2.http.*
import com.example.httpsrequest.data.UserResponse
import com.google.gson.JsonObject

interface myRetrofit {

    @GET("server/authentication-token/generate/{nick}")
    fun getAuthToken(
        @Path("nick") nick : String
    ) : Call<Token>

    @GET("server/transaction/all")
    fun getTransaction() : Call<MutableList<TransactionResponse>>

    @POST("server/user/register")
    fun userRegister(@Body userRegister : JsonObject) : Call<UserResponse>

    @POST("server/user/login")
    fun userLogin(@Body userLogin : JsonObject) : Call<UserResponse>

    @GET("server/validation-token/generate/{nick}")
    fun getValToken(
        @Path("nick") nick : String
    ) : Call<Token>

    @POST("server/transaction/sign")
    fun signTransaction(@Body transaction : JsonObject) : Call<TransactionResponse>
}