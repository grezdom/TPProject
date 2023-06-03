package com.example.httpsrequest.viewModels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.httpsrequest.data.*
import com.example.httpsrequest.retrofitapi.myRetrofit
import com.example.httpsrequest.retrofitapi.retrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityViewModel : ViewModel() {
    var reclerListData : MutableLiveData<MutableList<TransactionResponse>> = MutableLiveData()
    var liveNonceData : MutableLiveData<Token> = MutableLiveData()
    var signTranData : MutableLiveData<TransactionResponse> = MutableLiveData()

    fun signTransactionDataObservable(transactionItem: TransactionResponse, token : Token, signedNonce : ByteArray) : MutableLiveData<TransactionResponse> {
        signTransaction(transactionItem, token, signedNonce)
        return signTranData
    }

    fun getTransactionListObservarable() : MutableLiveData<MutableList<TransactionResponse>>{
        getTransactionList()
        return reclerListData
    }

    fun getLoginNonceDataObservable(nick : String) : MutableLiveData<Token> {
        getNonce(nick)
        return liveNonceData
    }

    private fun getNonce(nick : String) {
        val retroInstance = retrofitInstance.getRetroInstance().create(myRetrofit::class.java)
        val call = retroInstance.getAuthToken(nick)
        Log.d("TESTING", call.toString())
        call.enqueue(object : Callback<Token>{
            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                if(response.isSuccessful){
                    val responseBody = response.body()
                    if(responseBody != null){
                        liveNonceData.postValue(responseBody)
                    }else{
                        liveNonceData.postValue(Token(-1,-1,"","","Failed"))
                    }
                }else{
                    liveNonceData.postValue(Token(-1,-1,"","","Failed"))
                }
            }

            override fun onFailure(call: Call<Token>, t: Throwable) {
                liveNonceData.postValue(Token(-1,-1,"","","Failed"))
            }

        })
    }

    private fun getTransactionList() {
        val retroInstance = retrofitInstance.getRetroInstance().create(myRetrofit::class.java)
        val call = retroInstance.getTransaction()
        call.enqueue(object: Callback<MutableList<TransactionResponse>>{
            override fun onResponse(
                call: Call<MutableList<TransactionResponse>>,
                response: Response<MutableList<TransactionResponse>>
            ) {
                if(response.isSuccessful){
                    reclerListData.postValue(response.body())
                }else{
                    reclerListData.postValue(null)
                }
            }

            override fun onFailure(call: Call<MutableList<TransactionResponse>>, t: Throwable) {
                reclerListData.postValue(null)
            }


        })
    }


    private fun signTransaction(transactionItem : TransactionResponse, token : Token, signedNonce : ByteArray){
        val retroInstance = retrofitInstance.getRetroInstance().create(myRetrofit::class.java)
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", transactionItem.type)
        jsonObject.addProperty("volume", transactionItem.volume)
        jsonObject.addProperty("cbAccessKey","bla")
        jsonObject.addProperty("cbSecret", "bla")
        jsonObject.addProperty("topSpread",4)
        jsonObject.addProperty("targetPrice",transactionItem.targetPrice)
        jsonObject.addProperty("bottomSpread", 4)
        jsonObject.addProperty("asset", transactionItem.asset)
        jsonObject.addProperty("walletAddress",transactionItem.walletAddress)
        jsonObject.addProperty("validationTokenId", token.id)
        jsonObject.addProperty("tokenSignature",Base64.encodeToString(signedNonce,Base64.NO_WRAP))
        jsonObject.addProperty("releasedBy",transactionItem.releasedBy)

        val call = retroInstance.signTransaction(jsonObject)
        call.enqueue(object : Callback<TransactionResponse> {
            override fun onResponse(
                call: Call<TransactionResponse>,
                response: Response<TransactionResponse>
            ) {
                var myresponse = TransactionResponse(-1,"",-1,-1,"","",
                    -1,"", emptyList(),"Failed")
                if(response.isSuccessful){
                    myresponse = response.body()!!
                    signTranData.postValue(myresponse)
                }
                else{
                    signTranData.postValue(myresponse)
                }
            }

            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                signTranData.postValue(
                    TransactionResponse(-1,"",-1,-1,"","",
                -1,"", emptyList(),"Failed")
                )
            }

        })
    }

}