package com.bhaveshsp.talky.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.bhaveshsp.talky.adapter.MainAdapter
import com.bhaveshsp.talky.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth

/**
 * @author Bhavesh SP
 */
class MainActivity : AppCompatActivity() {
    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager2
    private lateinit var viewPage2Listener: ViewPager2.OnPageChangeCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.elevation = 0F
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        setUpTabLayout()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(viewPage2Listener)
    }

    private fun setUpTabLayout(){
        tabLayout.addTab(tabLayout.newTab().setText(R.string.chats))
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.status))
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.calls))
        tabLayout.setTabTextColors(Color.parseColor("#ffffff"),
            Color.parseColor("#ffffff"))
        val adapter = MainAdapter(this, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPage2Listener = object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewPager.currentItem = position
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        }
        viewPager.registerOnPageChangeCallback(viewPage2Listener)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.editItem -> {
                startActivity(Intent(this,EditProfileActivity::class.java))
                true
            }
            R.id.signOutItem -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this,LoginActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}