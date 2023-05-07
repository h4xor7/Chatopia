package com.panedey.chatopia.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.panedey.chatopia.databinding.ActivityRegisterBinding

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mauth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var mRegProgress: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mRegProgress = ProgressDialog(this)
        mauth = Firebase.auth
        bindHandlers()


    }

    private fun bindHandlers() {
        binding.regCreateBtn.setOnClickListener {
            val displayName = binding.registerDisplayName.text.toString()
            val email = binding.registerEmail.text.toString().trim()
            val password = binding.regPassword.text.toString().trim()

            if (!TextUtils.isEmpty(displayName) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(
                    password
                )
            ) {
                mRegProgress?.setTitle("Registering User");
                mRegProgress?.setMessage("Please wait while we create your account !");
                mRegProgress?.setCanceledOnTouchOutside(false);
                mRegProgress?.show();
                registerUser(displayName, email, password)
            }
        }
    }

    private fun registerUser(displayName: String, email: String, password: String) {
        mauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val uid = currentUser?.uid.toString()
                mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                val userMap = HashMap<String, String>()
                userMap["name"] = displayName
                userMap["status"] = "Hi there I'm using Chatopia."
                userMap["image"] = "default"
                userMap["thumb_image"] = "default"

                mDatabase.setValue(userMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mRegProgress?.dismiss()
                        val strtIntent = Intent(this@RegisterActivity, StartActivity::class.java)
                        startActivity(strtIntent)
                        finish()
                    }
                }

            } else {
                mRegProgress?.hide()
                Log.w("TAG", "createUserWithEmail:failure", task.exception)


            }
        }
    }
}