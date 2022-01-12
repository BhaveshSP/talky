package com.bhaveshsp.talky.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.bhaveshsp.talky.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.math.log

const val DATABASE_NAME = "users"
const val KEY_NAME = "name"
const val KEY_PHONE = "phone"
const val KEY_IMAGE = "image"
const val PICK_REQUEST_CODE = 20323
const val BUCKET = "talky-bdd01.appspot.com"

class EditProfileActivity : AppCompatActivity() {

    private var user: FirebaseUser? = null
    private lateinit var database : DatabaseReference
    private lateinit var userName : EditText
    private lateinit var userImage : ImageView
    private lateinit var progressBar : ProgressBar
    private var filePath : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar!!.title = getString(R.string.profile)


        userName = findViewById(R.id.userName)
        userImage = findViewById(R.id.userImage)
        progressBar = findViewById(R.id.uploadProgress)

        val doneButton : Button = findViewById(R.id.doneButton)
        getValuesFromDatabase()
        doneButton.setOnClickListener {
            val name : String = userName.text.toString()
            if (name.isNotEmpty()){
                // Update or Add User to Database
                addUserToDatabase(name)
            }
        }
        userImage.setOnClickListener {
            pickImage()
        }
    }
    private fun addUserToDatabase(name : String){
        progressBar.visibility = View.VISIBLE
        if(user!=null) {
            val storage = FirebaseStorage.getInstance()
            val imageName : String = UUID.randomUUID().toString()
            val reference : StorageReference = storage.reference.child("images/$imageName")
            if(filePath != null){
                // Upload File in Cloud Storage
                reference.putFile(filePath!!).addOnCompleteListener {
                    if (it.isSuccessful){
                        Log.d(TAG, "addUserToDatabase: File Uploaded")
                        Toast.makeText(this, "User Updated", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                    else{
                        Log.d(TAG, "addUserToDatabase: Error Happened ${it.exception}")
                    }
                }
            }

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditProfileActivity, "No Internet", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCancelled: No Internet Listener Cancelled")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        // Update the Values in Database
                        val userMap : MutableMap<String, Any> = mutableMapOf()
                        userMap[KEY_NAME] = name
                        userMap[KEY_IMAGE] = imageName
                        Log.d(TAG, "onDataChange: User Name $name")
                        Log.d(TAG, "onDataChange: File Path $filePath")
                        database.updateChildren(userMap)
                    }else{
                        Log.d(TAG, "onDataChange: Can Not Add Data Snap Shot Null")
                        Toast.makeText(this@EditProfileActivity, "Cannot Add Data", Toast.LENGTH_SHORT).show()
                    }
                }

            })
            // Goto Next Activity
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
        else{
            Log.d(TAG, "addUserToDatabase: User Null")
            Toast.makeText(this, "Error User Null", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }

    }
    private fun getValuesFromDatabase(){
        // Get the User and Image From the Database
        Log.d(TAG, "getValuesFromDatabase: Get User Name and Image")
        user  = FirebaseAuth.getInstance().currentUser
        if (user != null){
            database = FirebaseDatabase.getInstance().reference.child(DATABASE_NAME).child(user!!.uid)
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        userName.setText(snapshot.child(KEY_NAME).value.toString())
                        if (snapshot.hasChild(KEY_IMAGE)){
                            val imageName : String = snapshot.child(KEY_IMAGE).value.toString()
                            val imageReference : StorageReference= FirebaseStorage.getInstance().getReferenceFromUrl(getString(R.string.image_url, BUCKET,imageName))
                            imageReference.downloadUrl.addOnCompleteListener { 
                                if(it.isSuccessful){
                                    Glide.with(this@EditProfileActivity).load(it.result).into(userImage)
                                    Log.d(TAG, "onDataChange: Image Loaded")
                                }else{
                                    Log.d(TAG, "onDataChange: Image Not Loaded ${it.exception}")
                                }
                            }
                            
                            Log.d(TAG, "onDataChange: Image Url :")
                        }
                        else{
                            Log.d(TAG, "onDataChange: Snap Does not Have Image")
                        }
                    }else{
                        Log.d(TAG, "onDataChange: Cannot Get Data Snap is Null")
                        Toast.makeText(this@EditProfileActivity, "Cannot Get Data", Toast.LENGTH_SHORT).show()
                    }

                    progressBar.visibility = View.GONE
                }
            })
        }

    }

    private fun pickImage(){
        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_REQUEST_CODE) {
            filePath = data!!.data
            Log.d(TAG, "onActivityResult: File Path $filePath")
            userImage.setImageURI(filePath)
        }
    }
}