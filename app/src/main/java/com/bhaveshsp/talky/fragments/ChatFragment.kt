package com.bhaveshsp.talky.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.activities.*
import com.bhaveshsp.talky.adapter.CHAT
import com.bhaveshsp.talky.adapter.ChatListAdapter
import com.bhaveshsp.talky.models.Chat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton
    private var chatsList : ArrayList<Chat> = arrayListOf()
    private var chatNames : ArrayList<String> = arrayListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.chatListRecyclerView)
        floatingActionButton = view.findViewById(R.id.floatingButton)
        floatingActionButton.setOnClickListener{
            startActivity(Intent(activity!!,FindUserActivity::class.java))
        }
        setUpChatListAdapter()
    }
    private fun setUpChatListAdapter(){
        val adapter = ChatListAdapter(activity!!,chatsList)
        recyclerView.adapter = adapter
        val database : DatabaseReference =  FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(FirebaseAuth.getInstance().uid!!).child(
            CHAT)
        database.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for(i in snapshot.children){
                        val chat = Chat()
                        chat.chatId = i.key.toString()
                        for (j in i.children){
                            chat.name = j.key.toString()

                        }
                        if (chat.name in chatNames) {
                            continue
                        }else{
                            chatNames.add(chat.name)
                            chatsList.add(chat)
                        }
                        adapter.userChats = chatsList
                        adapter.notifyDataSetChanged()
                        Log.d(TAG, "onDataChange: Chat ${chat.chatId}")
                    }
                    Log.d(TAG, "onDataChange: User Has Chats $chatsList")
                }
                else{
                    Log.d(TAG, "onDataChange: User Has No Chats")
                }
            }
        })

    }
}