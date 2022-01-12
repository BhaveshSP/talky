package com.bhaveshsp.talky.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.adapter.CHAT
import com.bhaveshsp.talky.adapter.ChatAdapter
import com.bhaveshsp.talky.adapter.KEY_CHAT_ID
import com.bhaveshsp.talky.adapter.KEY_CHAT_NAME
import com.bhaveshsp.talky.models.Message
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

const val CHAT_DATABASE = "chats"
const val CHAT_ID = "chatId"
const val MESSAGE_KEY = "message"
const val SENDER_ID_KEY = "senderId"
const val SENDER_NAME = "senderName"
class ChatActivity : AppCompatActivity() {

    private var chatId :String? = null
    private var senderName : String? = null
    private val senderId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var firebaseDatabase : DatabaseReference
//    private var messageId : String = ""
    private lateinit var message : String
    private lateinit var sendButton : FloatingActionButton
    private lateinit var messageInput : EditText
    private lateinit var chatRecyclerView: RecyclerView
    private var messageList : ArrayList<Message> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        chatId = intent.getStringExtra(KEY_CHAT_ID)
        val name = intent.getStringExtra(KEY_CHAT_NAME)
        supportActionBar!!.title = name
        sendButton = findViewById(R.id.sendButton)
        messageInput = findViewById(R.id.messageInput)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        firebaseDatabase =
                FirebaseDatabase.getInstance().getReference(CHAT_DATABASE).child(CHAT_ID).child(chatId!!)
        sendButton.setOnClickListener {
            sendMessage()
        }
        getChats()
    }

    private fun getChats(){

        val adapter = ChatAdapter(this,messageList)
        chatRecyclerView.adapter = adapter

        // Get UserName
        val databaseReference : DatabaseReference  = FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(senderId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    senderName = snapshot.child(KEY_NAME).value.toString()
                }
                else{
                    Log.d(TAG, "onDataChange: Snap Does Not Exists")
                }
            }
        })

        // Get Message as Soon as New Message is Added to the Database
        val database :DatabaseReference = FirebaseDatabase.getInstance().getReference(CHAT_DATABASE).child(CHAT_ID).child(chatId!!)
        database.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
//                    Log.d(TAG, "onChildAdded: ${snapshot.child(messageId).value}")
//                    for (i in snapshot.children){
//                        Log.d(TAG, "onChildAdded: ${i.value}")
//                    }
                    val message = Message()
                    if (snapshot.hasChild(MESSAGE_KEY)) {
                        message.message = snapshot.child(MESSAGE_KEY).value.toString()
                    }
                    if (snapshot.hasChild(SENDER_NAME)) {
                        message.senderName = snapshot.child(SENDER_NAME).value.toString()
                    }
                    if (snapshot.hasChild(SENDER_ID_KEY)) {
                        message.senderId = snapshot.child(SENDER_ID_KEY).value.toString()
                    }
//                    Log.d(TAG, "onChildAdded: Child Added ${message.senderName}")
//                    Log.d(TAG, "onChildAdded: Child Added ${message.senderId}")
//                    Log.d(TAG, "onChildAdded: Child Added ${message.message}")

                    messageList.add(message)
                    adapter.messageList = messageList
                    adapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(messageList.size -1 )
                }else{
                    Log.d(TAG, "onChildAdded: Snap Message Not ")
                }
            }

            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}


            override fun onChildRemoved(snapshot: DataSnapshot) {}

        })
    }

    private fun sendMessage(){
        // Add Message to the Database
        message = messageInput.text.toString()
        if(message.isNotEmpty()) {

            val chatDatabase : DatabaseReference = firebaseDatabase.push()
            val mutableMap: MutableMap<String, Any> = mutableMapOf()
            mutableMap[MESSAGE_KEY] = message
            mutableMap[SENDER_ID_KEY] = senderId
            if (senderName !=null){
                mutableMap[SENDER_NAME] = senderName!!
            }
            chatDatabase.updateChildren(mutableMap)

            messageInput.setText("")
        }
    }
}