package com.bhaveshsp.talky.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bhaveshsp.talky.fragments.CallFragment
import com.bhaveshsp.talky.fragments.ChatFragment
import com.bhaveshsp.talky.fragments.StatusFragment

/**
 * @author Bhavesh SP
 */
class MainAdapter(activity: AppCompatActivity,private val totalTabs : Int) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return totalTabs
    }

    override fun createFragment(position: Int): Fragment {
//        return when(position){
//            0 -> ChatFragment()
//            1 -> StatusFragment()
//            else -> CallFragment()
//        }
        return ChatFragment()
    }

}