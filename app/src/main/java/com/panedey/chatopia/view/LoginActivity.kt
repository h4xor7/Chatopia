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
import com.google.firebase.ktx.Firebase
import com.panedey.chatopia.databinding.ActivityLoginBinding


@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mLoginProgress: ProgressDialog? = null
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        mLoginProgress = ProgressDialog(this)
        bindHandlers()
    }

    private fun bindHandlers() {
        binding.loginBtn.setOnClickListener {
            val email = binding.loginEmail.editText?.text.toString().trim()
            val password = binding.loginPassword.editText?.text.toString().trim()
            if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                mLoginProgress?.setTitle("Logging In")
                mLoginProgress?.setMessage("Please wait while we check your credentials.");
                mLoginProgress?.setCanceledOnTouchOutside(false)
                mLoginProgress?.show()

                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d(TAG, "loginUser:  email $email password $password")
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mLoginProgress?.dismiss();
                Log.d(Companion.TAG, "loginUser: Success")
                val user = auth.currentUser
                Log.d(TAG, "loginUser: user $user")
                val strtIntent = Intent(this@LoginActivity, StartActivity::class.java)
                startActivity(strtIntent)
                finish()

            } else {
                mLoginProgress?.hide();
                Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                Toast.makeText(
                    baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

    companion object {
        private const val TAG = "LoginActivity"
    }


}