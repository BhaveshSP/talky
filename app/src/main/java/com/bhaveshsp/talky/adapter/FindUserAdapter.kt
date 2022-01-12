package com.bhaveshsp.talky.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.activities.*
import com.bhaveshsp.talky.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

const val KEY_CHAT_ID = "chat_uid"
const val KEY_CHAT_NAME ="chat_name"
const val CHAT = "chat"
class FindUserAdapter(
    val context: Context,
    var users: ArrayList<User>
) : RecyclerView.Adapter<FindUserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.user_item_layout,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user : User = users[position]
        holder.userName.text = user.userName
        // If User Uploaded Image Fitch it
        if(user.userImage !=null){
            val reference : StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(context.getString(R.string.image_url, BUCKET,user.userImage))
            reference.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful){
                    Glide.with(context).load(it.result).into(holder.userImage)
                }else{
                    Log.d(TAG, "getImageUri: Image Not Loaded For User ${user.userImage}")
                    Log.d(TAG, "onBindViewHolder: ${it.exception}")
                }
            }

        }else{
            Log.d(TAG, "onBindViewHolder: user Does not have image ${user.userName}")
        }
        // Go to Unique Chat with User
        holder.layout.setOnClickListener {
            val database = FirebaseDatabase.getInstance().reference
            // Go to Unique Id in Chat Database
            val uniqueKey : String? = database.child(CHAT_DATABASE).push().key
            val userUid = FirebaseAuth.getInstance().uid!!
            val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(userUid)
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        database.child(DATABASE_NAME).child(user.uId).child(CHAT).child(uniqueKey!!).child(snapshot.child(KEY_NAME).value.toString()).setValue(true)
                    }
                    else{
                        Log.d(TAG, "onDataChange: Error in Setting User Name")
                    }
                }
            })

            // Add this key to both sides users
            database.child(DATABASE_NAME).child(userUid).child(CHAT).child(uniqueKey!!).child(user.userName).setValue(true)

            Log.d(TAG, "onBindViewHolder: User UId ${user.uId}")
            (context as Activity).finish()
        }



    }
    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val userName : TextView = itemView.findViewById(R.id.findUserName)
        val userImage : ImageView = itemView.findViewById(R.id.findUserImage)
        val layout : ConstraintLayout = itemView.findViewById(R.id.findUserLayout)
    }
}