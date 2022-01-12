package com.bhaveshsp.talky

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bhaveshsp.talky.activities.LoginActivity
import com.bhaveshsp.talky.activities.MainActivity
import java.io.IOException

/**
 * @author Bhavesh SP
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Thread{
            try {
                Thread.sleep(2500)
                startActivity(Intent(this,
                    LoginActivity::class.java))
                finish()
            }catch (e : IOException){
                e.printStackTrace()
            }
        }.start()
    }
}