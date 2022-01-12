package com.bhaveshsp.talky.adapter

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.activities.TAG
import com.bhaveshsp.talky.models.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(val context: Context, var messageList : ArrayList<Message>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.message_item_layout,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message : Message = messageList[position]
        Log.d(TAG, "onBindViewHolder: Message $message")
        if (FirebaseAuth.getInstance().currentUser!!.uid == message.senderId){
//            val layoutParams : LinearLayout.LayoutParams = holder.messageLayoutCard.layoutParams as LinearLayout.LayoutParams
//            layoutParams.gravity = Gravity.END
//            holder.messageLayoutCard.layoutParams = layoutParams
            holder.userSenderName.text = context.getString(R.string.you)
            holder.userMessage.text = message.message
            holder.messageLayoutCard.visibility = View.GONE
            holder.userMessageLayoutCard.visibility = View.VISIBLE
            Log.d(TAG, "onBindViewHolder: Message Sent by You")
        }
        else{
            holder.senderName.text = message.senderName
            holder.message.text = message.message
            holder.userMessageLayoutCard.visibility = View.GONE
            holder.messageLayoutCard.visibility = View.VISIBLE

            Log.d(TAG, "onBindViewHolder: Message Sent by Other")
        }
    }


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val messageLayoutCard : CardView = itemView.findViewById(R.id.cardView)
        val senderName : TextView = itemView.findViewById(R.id.senderName)
        val message : TextView = itemView.findViewById(R.id.messageText)

        val userMessageLayoutCard : CardView = itemView.findViewById(R.id.userCardView)
        val userSenderName : TextView = itemView.findViewById(R.id.userSenderName)
        val userMessage : TextView = itemView.findViewById(R.id.userMessageText)
    }

}