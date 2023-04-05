package com.panedey.chatopia

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.panedey.chatopia.databinding.ActivityMainBinding
import com.panedey.chatopia.view.LoginActivity
import com.panedey.chatopia.view.RegisterActivity
import com.panedey.chatopia.view.StartActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = Firebase.auth
        bindHandlers()
    }

    private fun bindHandlers() {
        binding.startRegBtn.setOnClickListener {
            val regIntent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(regIntent)
            finish()
        }


        binding.startLoginBtn.setOnClickListener {
            val logIntent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(logIntent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null){
            val logIntent = Intent(this@MainActivity, StartActivity::class.java)
            startActivity(logIntent)
            finish()
        }
    }
}