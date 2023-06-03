package com.example.httpsrequest

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.httpsrequest.data.publicKeySlots
import com.example.httpsrequest.data.publicKeys
import com.example.httpsrequest.data.UserResponse
import com.example.httpsrequest.databinding.ActivityRegisterBinding
import com.example.httpsrequest.piv.pivRegPublicKeyContract
import com.example.httpsrequest.viewModels.RegisterActivityViewModel
import com.yubico.yubikit.piv.Slot
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

private const val SIGN_PK = "SIGN_PK"
private const val AUTH_PK = "AUTH_PK"

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private var publicKeys : publicKeys? = null
    private lateinit var viewModel : RegisterActivityViewModel
    private lateinit var nickName : String

    val requestPublicKey = registerForActivityResult(pivRegPublicKeyContract()){
        publicKeys = it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Register"
        binding.registerImportPKs.setOnClickListener { importPKs() }
        binding.registerRegisterBtn.setOnClickListener { validateUser() }
        nickFocusListener()
    }

    private fun nickFocusListener() {
        binding.regiserNick.setOnFocusChangeListener { _, focused ->
            if(!focused){
                binding.registerNickLayout.helperText = validateNick()
            }
        }
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this).get(RegisterActivityViewModel::class.java)
        viewModel.postLiveRegisterDataObservable(nickName, publicKeys!!).observe(this,
        Observer<UserResponse>{
            if(it.status != "Failed"){
                if(it.status == "New") {
                    Log.d("REGISTERPK1", publicKeys!!.authPublicKey.encoded.toString())
                    Log.d("REGISTERPK2", publicKeys!!.signPublicKey.encoded.toString())
                    registerUser()
                }
                else{
                    binding.registerNickLayout.helperText = "User with that name exists"
                }
            }else{
                binding.registerNickLayout.helperText = "Something Went Wrong"
            }
        })
    }

    private fun validateNick() : String?{
        val nickText = binding.regiserNick.text.toString()
        if(nickText.length < 5){
            return "Minimum 5 Character Nick"
        }
        else if(nickText.length > 16){
            return "Maximum input is 16 Characters"
        }
        else if(nickText.matches(".*[!@#\$%*+?|].*".toRegex())){
            return "It Cannot Contain these Characters"
        }
        return null
    }

    private fun importPKs() {
        if(publicKeys == null){
            val mySlots = publicKeySlots(Slot.AUTHENTICATION,Slot.SIGNATURE)
            requestPublicKey.launch(mySlots)
        }else{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("IMPORT MASSAGE")
            builder.setMessage("KEYS WERE IMPORTED SUCCESSFULLY")
            builder.show()
        }
    }
//
//    private fun loadPK(){
//        if(publicKeys == null){
//            return
//        }
//        getPreferences(MODE_PRIVATE).getString(SIGN_PK,null)?.let {
//            val bytes = Base64.decode(it, Base64.DEFAULT)
//            val kf = KeyFactory.getInstance("RSA")
//            val spec = X509EncodedKeySpec(bytes)
//            publicKeys!!.signPublicKey = kf.generatePublic(spec)
//        }
//        getPreferences(MODE_PRIVATE).getString(AUTH_PK,null)?.let {
//            val bytes = Base64.decode(it, Base64.DEFAULT)
//            val kf = KeyFactory.getInstance("RSA")
//            val spec = X509EncodedKeySpec(bytes)
//            publicKeys!!.authPublicKey = kf.generatePublic(spec)
//        }
//    }
//
//    private fun savePK(){
//        publicKeys?.let {
//            getPreferences(MODE_PRIVATE).edit()
//                .putString(SIGN_PK, Base64.encodeToString(it.signPublicKey.encoded, Base64.DEFAULT))
//                .putString(AUTH_PK, Base64.encodeToString(it.authPublicKey.encoded, Base64.DEFAULT))
//                .apply()
//        }
//    }
//
//    override fun onPause() {
//        savePK()
//        super.onPause()
//    }
//
//    override fun onResume() {
//        loadPK()
//        super.onResume()
//    }

    private fun validateUser() {
        binding.registerNickLayout.helperText = validateNick()
        val validNickName = binding.registerNickLayout.helperText == null
        if(publicKeys != null && validNickName){
            nickName = binding.regiserNick.text.toString()
            initViewModel()
        }
    }

    private fun registerUser(){
        val preferences : SharedPreferences = getSharedPreferences("DEMO_APP", MODE_PRIVATE)
        preferences.edit().putString("USER", binding.regiserNick.text.toString()).apply()
        startActivity(Intent(this,MainActivity()::class.java))
    }

}