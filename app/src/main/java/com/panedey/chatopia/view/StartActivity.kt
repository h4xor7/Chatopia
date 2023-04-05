package com.panedey.chatopia.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.panedey.chatopia.MainActivity
import com.panedey.chatopia.R
import com.panedey.chatopia.databinding.ActivityStartBinding


class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    private lateinit var auth: FirebaseAuth;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //  setSupportActionBar(binding.mainPageToolbar as Toolbar)
        setSupportActionBar(binding.mainPageToolbar)
        supportActionBar?.title = "Chatopia"

        val adapter = SectionsPagerAdapter(supportFragmentManager)
        adapter.addFragment(ChatFragment(), "CHATS")
        adapter.addFragment(FriendsFragment(), "FRIENDS")
        binding.mainTabPager.adapter = adapter

        binding.mainTabs.setupWithViewPager(binding.mainTabPager)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.main_settings_btn -> {
                val settingsIntent = Intent(this@StartActivity, SettingsActivity::class.java)
                startActivity(settingsIntent)
                return true
            }
            R.id.main_all_btn -> {
                val usersIntent = Intent(this@StartActivity, UsersActivity::class.java)
                startActivity(usersIntent)
                return true
            }
            R.id.main_logout_btn -> {
                FirebaseAuth.getInstance().signOut();
                sendToMain();
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun sendToMain() {
        val logoutIntent = Intent(this@StartActivity, MainActivity::class.java)
        startActivity(logoutIntent)
    }

}