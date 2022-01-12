package com.bhaveshsp.talky.activities

import android.Manifest
import android.database.Cursor
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bhaveshsp.talky.R
import com.bhaveshsp.talky.models.User
import com.bhaveshsp.talky.adapter.FindUserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

const val READ_PERMISSION_CODE = 1213
class FindUserActivity : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView
    private var users : ArrayList<User> = arrayListOf()
    private var contacts : ArrayList<User> = arrayListOf()
    private lateinit var adapter : FindUserAdapter
    private lateinit var progress : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), READ_PERMISSION_CODE)
        }
        supportActionBar!!.title = getString(R.string.find_friends)
        progress = findViewById(R.id.findProgress)
        recyclerView = findViewById(R.id.findUsersRecyclerView)
        setUpAdapter()
    }

    private fun setUpAdapter(){
        adapter = FindUserAdapter(this,users)
        recyclerView.adapter = adapter
        val cursor : Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                null,null,null)
        if (cursor == null) {
            Log.d(TAG, "setUpAdapter: Cursor Null No Contacts")
            return
        }
        var lastNumber  = ""
        while (cursor.moveToNext()){
            val name  = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            phoneNumber = phoneNumber.replace("+","")
            phoneNumber = phoneNumber.replace("-","")
            phoneNumber = phoneNumber.replace("(","")
            phoneNumber = phoneNumber.replace(")","")
            phoneNumber = phoneNumber.replace(" ","")
            if (lastNumber == phoneNumber)
                continue
            lastNumber = phoneNumber
            val user = User()
            user.userName = name
            user.phone = phoneNumber
            contacts.add(user)
            checkIfUserInDatabase(user)

        }
//        Log.d(TAG, "setUpAdapter: Contacts $contacts")
//        adapter.users = users
//        adapter.notifyDataSetChanged()
        cursor.close()
    }
    private fun checkIfUserInDatabase(contact: User){
        val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference(DATABASE_NAME)
        // Find the User Which is Also a User in Database
        val query : Query = databaseReference.orderByChild(KEY_PHONE).equalTo(contact.phone)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Query Failed")
                Toast.makeText(this@FindUserActivity, "No Internet", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    Log.d(TAG, "setUpAdapter: phone:${contact.phone}")
                    val userSnap = User()
                    for (i in snapshot.children) {
                        if (i.key.toString() != FirebaseAuth.getInstance().currentUser!!.uid) {
                            userSnap.uId = i.key.toString()

                            if (i.child(KEY_PHONE).value != null) {
                                userSnap.phone = i.child(KEY_PHONE).value.toString()
                            }
                            if (i.child(KEY_NAME).value != null) {
                                userSnap.userName = i.child(KEY_NAME).value.toString()
                            }
                            if (i.hasChild(KEY_IMAGE)) {
                                userSnap.userImage = i.child(KEY_IMAGE).value.toString()
                            } else {
                                userSnap.userImage = null
                            }
                            progress.visibility = View.GONE
//                        if (userSnap in users){
                            users.add(userSnap) // Add User Which is Also a User in Database
//                        }
                        }
                        Log.d(TAG, "onDataChange: Snap Exists ${snapshot.value}")
//                    Log.d(TAG, "onDataChange: Snap ${snapshot.ref}")
                        // Update List in Adapter
                        adapter.users = users
                        adapter.notifyDataSetChanged()
                    }
                }
//                else{
////                    Log.d(TAG, "onDataChange: Snap Does not Exist ${snapshot.value}")
////                    Log.d(TAG, "onDataChange: Snap not ${snapshot.ref}")
////                    Toast.makeText(this@FindUserActivity, "Snap Not Exist", Toast.LENGTH_SHORT).show()
//                }
            }

        })
    }

}