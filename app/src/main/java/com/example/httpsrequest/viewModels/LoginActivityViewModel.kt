package com.example.httpsrequest.viewModels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.httpsrequest.data.*
import com.example.httpsrequest.retrofitapi.myRetrofit
import com.example.httpsrequest.retrofitapi.retrofitInstance
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivityViewModel : ViewModel() {

    var liveLoginData : MutableLiveData<UserResponse> = MutableLiveData()
    var liveNonceData : MutableLiveData<Token> = MutableLiveData()

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

    fun postLoginLiveDataObservable(nick : String, tokenId : Long, signedNonce: ByteArray)
            : MutableLiveData<UserResponse> {
        postLogin(nick, tokenId, signedNonce)
        return liveLoginData
    }

    private fun postLogin(nick: String, tokenId: Long, signedNonce : ByteArray) {
        val retroInstance = retrofitInstance.getRetroInstance().create(myRetrofit::class.java)
        val userLogin = userRequestLogin(nick,tokenId,Base64.encodeToString(signedNonce,Base64.NO_WRAP))
        val userRequestLogin = JsonObject()
        Log.d("SignatureSPKBase64", tokenId.toString())//Signature + FLag
        Log.d("SignatureSPKBaseConf", userLogin.toString())//Signature + FLag
        userRequestLogin.addProperty("nick", userLogin.nick)
        userRequestLogin.addProperty("authenticationTokenId", userLogin.authenticationTokenId)
        userRequestLogin.addProperty("tokenSignature", userLogin.tokenSignature)

        val call = retroInstance.userLogin(userRequestLogin)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                var myresponse = UserResponse("","","","Failed")
                if(response.isSuccessful){
                    myresponse = response.body()!!
                    liveLoginData.postValue(myresponse)
                    }
                else{
                    liveLoginData.postValue(myresponse)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                liveLoginData.postValue(UserResponse("","","","Failed"))
            }

        })
    }
}