package com.example.httpsrequest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.httpsrequest.R
import com.example.httpsrequest.data.TransactionItem
import com.example.httpsrequest.data.TransactionResponse

class TransactionViewAdapter(name : String, onClickListenerAdapter: onClickListener)
    : RecyclerView.Adapter<TransactionViewAdapter.ViewHolder>() {

    var tranList = mutableListOf<TransactionResponse>()
    var userNick : String = name
    private var monClickListener : onClickListener = onClickListenerAdapter

    class ViewHolder(itemView: View, private var onClickListener: onClickListener, context : Context, userNick : String):
        RecyclerView.ViewHolder(itemView){
        private lateinit var myTransaction : TransactionResponse
        private val myContext = context
        private val authorName : TextView = itemView.findViewById(R.id.tran_authorName)
        private val type : TextView = itemView.findViewById(R.id.tran_type)
        private val assetImg : ImageView = itemView.findViewById(R.id.tran_asset)
        private val volume : TextView = itemView.findViewById(R.id.tran_volume)
        private val myLayout : LinearLayout = itemView.findViewById(R.id.tran_item_layout)
        private val targetPrice : TextView = itemView.findViewById(R.id.tran_targetPrice)
        private val wallAddress : TextView = itemView.findViewById(R.id.tran_address)
        private val myExpandableLayout : ConstraintLayout = itemView.findViewById(R.id.tran_item_expendable_layout)
        private val tranStatus : TextView = itemView.findViewById(R.id.tran_status)
        private val tranSign : Button = itemView.findViewById(R.id.tran_sign)
        private val userNick = userNick


        init {
            super.itemView
            myLayout.setOnClickListener { expandLayout() }
            tranSign.setOnClickListener { signTransaction() }
        }

        private fun signTransaction() {
            onClickListener.signTransaction(myTransaction)
        }

        private fun expandLayout() {
            if(myExpandableLayout.visibility.equals(View.GONE)){
                if (userNick in myTransaction.validators){
                    tranSign.text = "Signed"
                    tranSign.isClickable = false
                }else{
                    tranSign.text = "Sign"
                    tranSign.isClickable = true
                }
                myExpandableLayout.visibility = View.VISIBLE
            }else{
                myExpandableLayout.visibility = View.GONE

            }
        }

        fun bind(data : TransactionResponse){
            myTransaction = data
            authorName.text = myTransaction.releasedBy
            type.text = myTransaction.type
            volume.text = myTransaction.volume.toString()
            targetPrice.text = myTransaction.targetPrice.toString()
            wallAddress.text = myTransaction.walletAddress
            tranStatus.text = myTransaction.status
            if(myTransaction.asset.equals("ETH")) assetImg.setImageResource(R.mipmap.tran_eth)

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view, monClickListener, parent.context, userNick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tranList[position])
    }

    override fun getItemCount(): Int {
        return tranList.size
    }

    interface onClickListener{
        fun signTransaction(transaction : TransactionResponse)
    }

}