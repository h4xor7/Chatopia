package com.panedey.chatopia.view

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.panedey.chatopia.databinding.ActivityStatusBinding
import com.panedey.chatopia.utils.Constants.USER_REF

@Suppress("DEPRECATION")
class StatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatusBinding
    private var mStatusDatabase: DatabaseReference? = null
    private var mCurrentUser: FirebaseUser? = null


    //Progress
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        mCurrentUser = FirebaseAuth.getInstance().currentUser
        val currentUid = mCurrentUser!!.uid

        mStatusDatabase = FirebaseDatabase.getInstance().reference.child(USER_REF).child(currentUid)

        setSupportActionBar(binding.statusAppBar);
        supportActionBar?.title = "Account Status";
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        val statusValue = intent.getStringExtra("status_value")
        binding.statusInput.editText?.setText(statusValue.toString())


        binding.statusSaveBtn.setOnClickListener {
            mProgress = ProgressDialog(this)
            mProgress!!.setTitle("Saving Changes")
            mProgress!!.setMessage("Please wait while we save the changes")
            mProgress!!.setCanceledOnTouchOutside(false)
            mProgress!!.show()

            val status: String = binding.statusInput.editText?.text.toString()
            mStatusDatabase!!.child("status").setValue(status).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    mProgress?.dismiss()
                    onBackPressed()
                } else {
                    Toast.makeText(applicationContext, "There was some error in saving Changes.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}