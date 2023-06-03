package com.example.httpsrequest

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.httpsrequest.data.Token
import com.example.httpsrequest.data.UserResponse
import com.example.httpsrequest.databinding.ActivityLoginBinding
import com.example.httpsrequest.piv.pivSignMessageContract
import com.example.httpsrequest.viewModels.LoginActivityViewModel
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.Slot

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var viewModel : LoginActivityViewModel
    private var signedMessage : ByteArray? = null
    private var myLoginToken : Token? = null
    private var loginNick : String? = null

    val requestSignMessage = registerForActivityResult(pivSignMessageContract()){
        signedMessage = it
        if(signedMessage != null){
            loginRequest()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Log In"
        binding.loginLoginBtn.setOnClickListener { login() }
    }

    private  fun initNonce(nick : String){
        viewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)
        viewModel.getLoginNonceDataObservable(nick).observe(this,
            Observer<Token>{
                if(it.status != "Failed"){
                    myLoginToken = it
                    requestSignMessage.launch(Pair(Slot.AUTHENTICATION, myLoginToken!!.token))
                }else{
                    binding.loginNickLayout.helperText = "Request of Nonce Went Worng"
                    myLoginToken = null
                } })
    }

    private fun login() {
        if(binding.loginNick.text.toString() == ""){
            binding.loginNickLayout.helperText = "Please Enter your Nick name!"
        }
        else {
            loginNick = binding.loginNick.text.toString()
            initNonce(binding.loginNick.text.toString())
        }
    }

    private fun loginRequest(){
        viewModel.postLoginLiveDataObservable(binding.loginNick.text.toString()
            ,myLoginToken!!.id,signedMessage!!).observe(this,
            Observer<UserResponse>{
                if (it.status != "Failed"){
                    getSharedPreferences("DEMO_APP", MODE_PRIVATE).edit().putString("USER", loginNick).apply()
                    startActivity(Intent(this,MainActivity::class.java))
                }else{
                    binding.loginNickLayout.helperText = "Login Request Went Wrong"
                    myLoginToken = null
                    signedMessage = null
                }
            })
    }
}