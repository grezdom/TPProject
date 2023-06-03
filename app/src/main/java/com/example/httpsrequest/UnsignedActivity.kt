package com.example.httpsrequest

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.httpsrequest.piv.pivSignPublicKeyContract
import com.yubico.yubikit.piv.Slot
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec



class UnsignedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unsigned)
        val registerButton : Button = findViewById(R.id.unsigned_registerBtn)
        val loginButton : Button = findViewById(R.id.unsigned_loginBtn)
        registerButton.setOnClickListener { register() }
        loginButton.setOnClickListener { login() }
    }

    private fun login() {
        startActivity(Intent(this,LoginActivity::class.java))
    }


    private fun register() {
        startActivity(Intent(this,RegisterActivity::class.java))
    }


}