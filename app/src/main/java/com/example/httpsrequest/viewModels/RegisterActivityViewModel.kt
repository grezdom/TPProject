package com.example.httpsrequest.viewModels

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.httpsrequest.data.publicKeys
import com.example.httpsrequest.data.UserRequestRegister
import com.example.httpsrequest.data.UserResponse
import com.example.httpsrequest.retrofitapi.myRetrofit
import com.example.httpsrequest.retrofitapi.retrofitInstance
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivityViewModel : ViewModel() {
    var liveRegistrationData : MutableLiveData<UserResponse> = MutableLiveData()

    fun postLiveRegisterDataObservable(nick : String, keys : publicKeys)
    :MutableLiveData<UserResponse>{
        postRegistration(nick, keys)
        return liveRegistrationData
    }

    private fun retypeData(key: ByteArray) : String{
        return key.joinToString (""){
            String.format("%02x", it)
        }
    }


    private fun postRegistration(nick: String, keys: publicKeys) {
        val retroInstance = retrofitInstance.getRetroInstance().create(myRetrofit::class.java)
        val userRegister = UserRequestRegister(nick,
            Base64.encodeToString(keys.authPublicKey.encoded, Base64.NO_WRAP),
            Base64.encodeToString(keys.signPublicKey.encoded,Base64.NO_WRAP))
        Log.d("REGISTERPKHEX1", userRegister.authenticationPublicKey)
        Log.d("REGISTERPKHEX2", userRegister.validationPublicKey)//Validation is for signature
        val UserRequestRegister = JsonObject()
        UserRequestRegister.addProperty("nick", userRegister.nick)
        UserRequestRegister.addProperty("authenticationPublicKey", userRegister.authenticationPublicKey)
        UserRequestRegister.addProperty("validationPublicKey", userRegister.validationPublicKey)

        val call = retroInstance.userRegister(UserRequestRegister)
        call.enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                var myResponse = UserResponse("","","","Failed")
                if(response.isSuccessful){
                    myResponse = response.body()!!
                    liveRegistrationData.postValue(myResponse)
                }else{
                    liveRegistrationData.postValue(myResponse)
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                liveRegistrationData.postValue(UserResponse("","","","Failed"))
            }

        })
    }
}