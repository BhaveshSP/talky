package com.bhaveshsp.talky.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.activities.CHAT_DATABASE
import com.bhaveshsp.talky.activities.CHAT_ID
import com.bhaveshsp.talky.activities.ChatActivity
import com.bhaveshsp.talky.activities.TAG
import com.bhaveshsp.talky.models.Chat
import com.google.firebase.database.FirebaseDatabase

class ChatListAdapter(val context: Context,var userChats : ArrayList<Chat>) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.user_item_layout,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userChats.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chatName.text = userChats[position].name
        Log.d(TAG, "onBindViewHolder: holder ${userChats[position].name}")
        holder.layout.setOnClickListener {
//            val database = FirebaseDatabase.getInstance().getReference(CHAT_DATABASE).child(CHAT_ID).child(userChats[position].chatId)
//            database.setValue(true)
            context.startActivity(Intent(context,ChatActivity::class.java).putExtra(KEY_CHAT_ID,userChats[position].chatId)
                    .putExtra(KEY_CHAT_NAME,userChats[position].name))
        }
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val chatName : TextView = itemView.findViewById(R.id.findUserName)
        val layout : ConstraintLayout = itemView.findViewById(R.id.findUserLayout)

    }
}