package com.bhaveshsp.talky.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.text.isDigitsOnly
import com.bhaveshsp.talky.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

/**
 * @author Bhavesh SP
 */
const val TAG = "TESTING"
const val COUNTER_CODE = "91"
class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton : Button
    private lateinit var code : EditText
    private lateinit var phoneNumber : EditText
    private var verificationCode : String? = null
    private var phoneNo : String = ""
    private lateinit var numberText : TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var codeLayout : LinearLayout
    private lateinit var phoneLayout : LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseApp.initializeApp(this)
        // Initialize Components
        loginButton = findViewById(R.id.loginButton)
        code = findViewById(R.id.codeText)
        phoneNumber = findViewById(R.id.phoneNumberText)
        progressBar = findViewById(R.id.progressBarLogin)
        numberText = findViewById(R.id.phoneNumberChange)
        phoneLayout = findViewById(R.id.phoneNumberLayout)
        codeLayout = findViewById(R.id.codeLayout)


        checkIfUser() // Check if User Logged In

        loginButton.setOnClickListener {
            if (!checkIfPhoneNumberIsValid()){
                phoneNumber.setText("")
                Toast.makeText(this, getString(R.string.error_phone_number), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (verificationCode != null){
                progressBar.visibility = View.VISIBLE
                verifyCode()
                Log.d(TAG, "onCreate: Verification Code Not Null Verify code")
            }
            else{
                progressBar.visibility = View.VISIBLE
                sendVerificationCode()
                Log.d(TAG, "onCreate: Verification Code Null Send Code")
            }
            checkIfUser()
        }
        numberText.setOnClickListener {
            numberText.visibility = View.GONE
            phoneLayout.visibility = View.VISIBLE
            loginButton.setText(R.string.send_code)
            codeLayout.visibility = View.GONE
            verificationCode = null

        }

    }

    private fun checkIfPhoneNumberIsValid() : Boolean{
        val numberString = phoneNumber.text.toString()
        if ((numberString.isNotEmpty() ) and (numberString.length == 10)){
            if(numberString.isDigitsOnly()){
                phoneNo = getString(R.string.phone_number_placeholder, COUNTER_CODE,numberString)
                Log.d(TAG, "checkIfPhoneNumberIsValid: Valid Number")
                return true
            }
        }

        Log.d(TAG, "checkIfPhoneNumberIsValid: Invalid Number")
        return false
    }

    private fun sendVerificationCode(){
//         Set Phone Auth Options

        val options = PhoneAuthOptions.newBuilder()
            .setActivity(this)
            .setPhoneNumber(phoneNo)
            .setTimeout(60,TimeUnit.SECONDS)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    singInWithCredentials(p0)
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Log.d(TAG, "onVerificationFailed: ${p0.message}")
                    Log.d(TAG, "onVerificationFailed: $phoneNo")
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "No Internet Connection. Please Try Again", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    verificationCode = p0
                    loginButton.setText(R.string.verify)
                    codeLayout.visibility = View.VISIBLE
                    numberText.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    numberText.text = getString(R.string.change_phone_number,phoneNo)
                    phoneLayout.visibility = View.GONE
                }
            } )
            .build()
        // Verify Phone Number via options
        // Sent code to Verify
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private fun verifyCode(){
        // Create Credentials With VerificationId and  Code
        val phoneAuthCredential = PhoneAuthProvider.getCredential(verificationCode!!,code.text.toString())
        Log.d(TAG, "verifyCode: Verifying Code")
        singInWithCredentials(phoneAuthCredential)
    }

    private fun singInWithCredentials(phoneAuthCredential: PhoneAuthCredential) {
        // Sign In with Credentials
        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true) // Disable App Verification
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener { 
            if(it.isSuccessful){
                // Add User to Database
                val user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null){
                    val database : DatabaseReference = FirebaseDatabase.getInstance().getReference(DATABASE_NAME).child(user.uid)
                    database.setValue(true)
//                    val databaseReference : DatabaseReference = database.child(
//                            DATABASE_NAME)
                    database.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@LoginActivity, "No Internet", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "onCancelled: User Not Added $error")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val userMap : MutableMap<String,Any> = mutableMapOf()
                                userMap[KEY_NAME] = DATABASE_NAME
                                phoneNo = phoneNo.replace("+91","")
                                userMap[KEY_PHONE] = phoneNo
                                database.updateChildren(userMap)
                            }else{
                                Log.d(TAG, "onDataChange: $snapshot")
                                Log.d(TAG, "onDataChange: ${snapshot.ref}")
                                Log.d(TAG, "onDataChange: Snap Does not Exist")
                                Toast.makeText(this@LoginActivity, "Snap Does not Exist Check Database", Toast.LENGTH_SHORT).show()
                            }

                        }
                    })
                }

            }else{
                Log.d(TAG, "singInWithCredentials: ${it.exception}")
            }
            checkIfUser()
            Log.d(TAG, "singInWithCredentials: Signed In.....")
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(this@LoginActivity, getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "singInWithCredentials: Failed Wrong Code")
        }

    }



    private fun checkIfUser(){
        val user : FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (user != null){

            progressBar.visibility = View.GONE
            // Go to Next Activity
            startActivity(Intent(this,MainActivity::class.java))
            // Find Users
//            startActivity(Intent(this,FindUserActivity::class.java))

            // Profile Activity
//            startActivity(Intent(this,EditProfileActivity::class.java))
            finish()
        }
        return
    }

}