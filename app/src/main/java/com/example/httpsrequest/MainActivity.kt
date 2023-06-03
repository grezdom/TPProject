package com.example.httpsrequest

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.httpsrequest.adapter.TransactionViewAdapter
import com.example.httpsrequest.data.Token
import com.example.httpsrequest.data.TransactionItem
import com.example.httpsrequest.data.TransactionResponse
import com.example.httpsrequest.data.UserResponse
import com.example.httpsrequest.databinding.ActivityLoginBinding
import com.example.httpsrequest.databinding.ActivityMainBinding
import com.example.httpsrequest.piv.pivSignMessageContract
import com.example.httpsrequest.viewModels.LoginActivityViewModel
import com.example.httpsrequest.viewModels.MainActivityViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yubico.yubikit.core.util.Pair
import com.yubico.yubikit.piv.Slot


class MainActivity : AppCompatActivity(), TransactionViewAdapter.onClickListener {

    private lateinit var transactionViewAdapter: TransactionViewAdapter
    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel : MainActivityViewModel
    private lateinit var nickName : String
    private var signedMessage : ByteArray? = null
    private var requestedNonce : Token? = null
    private var toSignTransaction : TransactionResponse? = null


    val requestSignMessage = registerForActivityResult(pivSignMessageContract()){
        signedMessage = it
        if(signedMessage != null){
            signTheTransaction()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(isLoggedIn()){
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = nickName
            initRecyclerView()
            initViewModel()
            binding.mainSwipe.setOnRefreshListener {
                initViewModel()
                binding.mainSwipe.isRefreshing = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.getTransactionListObservarable().observe(this,Observer<MutableList<TransactionResponse>>{
            if(it != null){
                transactionViewAdapter.tranList = it
                transactionViewAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun initRecyclerView() {
        val myRecyclerView : RecyclerView = findViewById(R.id.main_recyclerView)
        val toolbarBotton : Button = findViewById(R.id.main_logout)

        myRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            val decoration = DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)
            addItemDecoration(decoration)
            transactionViewAdapter = TransactionViewAdapter(nickName, this@MainActivity)
            adapter = transactionViewAdapter
        }
        toolbarBotton.setOnClickListener{logout()}
    }


    private fun logout() {
        getSharedPreferences("DEMO_APP", MODE_PRIVATE).edit().putString("USER", null).apply()
        startActivity(Intent(this,UnsignedActivity::class.java))

    }



    private fun isLoggedIn() : Boolean{
        val preferences : SharedPreferences = getSharedPreferences("DEMO_APP", MODE_PRIVATE)
        val isLogged : String? = preferences.getString("USER", null)
        return if(isLogged == null){
            startActivity(Intent(this, UnsignedActivity()::class.java))
            false
        }else{
            nickName = isLogged
            true
        }
    }

    override fun signTransaction(transaction: TransactionResponse) {
        toSignTransaction = transaction
        getValNonce()
    }

    private fun getValNonce() {
        viewModel.getLoginNonceDataObservable(nickName).observe(this,
            Observer<Token>{
                if(it.status != "Failed"){
                    requestedNonce = it
                    requestSignMessage.launch(Pair(Slot.SIGNATURE, it.token))
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Fail")
                    builder.setMessage("Server Fail")
                    builder.show()
                } })
    }

    private fun signTheTransaction() {
        viewModel.signTransactionDataObservable(toSignTransaction!!, requestedNonce!!, signedMessage!!).observe(this,
            Observer<TransactionResponse>{
                if (it.status != "Failed"){
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Signature")
                    builder.setMessage("Signature of transaction was Successful")
                        .setPositiveButton("Yes") { _, _ ->
                            run {
                                finish()
                                startActivity(intent)
                            }
                        }
                    builder.show()
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Signature")
                    builder.setMessage("Signature of transaction was NOT Successful")
                    builder.show()
                    toSignTransaction = null
                    signedMessage = null
                }
            })
    }

}