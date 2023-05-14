package com.panedey.chatopia.view

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.panedey.chatopia.R
import com.panedey.chatopia.databinding.ActivitySettingsBinding
import com.panedey.chatopia.utils.Constants
import com.panedey.chatopia.utils.Constants.DISPLAY_NAME_REF
import com.panedey.chatopia.utils.Constants.IMAGE_REF
import com.panedey.chatopia.utils.Constants.STATUS_REF
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val GALLERY_PICK = 1

    private var mUserDatabase: DatabaseReference? = null
    private var mCurrentUser: FirebaseUser? = null
    private lateinit var uploadTask:UploadTask

    // Storage Firebase
    private var mImageStorage: StorageReference? = null

    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bindHandlers()

    }

    private fun bindHandlers() {
        mImageStorage = FirebaseStorage.getInstance().reference

        mCurrentUser = FirebaseAuth.getInstance().currentUser

        val currentUid: String = mCurrentUser?.uid!!

        mUserDatabase =
            FirebaseDatabase.getInstance().reference.child(Constants.USER_REF).child(currentUid)

        mUserDatabase?.keepSynced(true)

        mUserDatabase?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name: String = snapshot.child(DISPLAY_NAME_REF).value.toString()
                val image = snapshot.child(IMAGE_REF).value.toString()
                val status: String = snapshot.child(STATUS_REF).value.toString()
                val thumbImage: String = snapshot.child("thumb_image").value.toString()

                Log.d(TAG, "onDataChange: Image value $image")

                binding.settingsName.text = name
                binding.settingsStatus.text = status

                Picasso.get().load(image)
                    .placeholder(resources.getDrawable(R.drawable.default_avatar))
                    .into(binding.settingsImage)


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        binding.settingsStatusBtn.setOnClickListener {
            val statusValue: String = binding.settingsStatus.text.toString()

            val statusIntent = Intent(this@SettingsActivity, StatusActivity::class.java)
            statusIntent.putExtra("statusValue", statusValue)
            startActivity(statusIntent)
        }


        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d(Companion.TAG, "bindHandlers: Image aa gya ")

            CropImage.activity(uri)
                .setAspectRatio(1, 1)
                .setMinCropWindowSize(500, 500)
                .setOutputCompressQuality(50)
                .setMultiTouchEnabled(true)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this)
        }




        binding.settingsImageBtn.setOnClickListener {
            getContent.launch("image/*")
        }

    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = ""
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return fileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== RESULT_OK) {
            //ye image aa gya
            var result = CropImage.getActivityResult(data)
            binding.settingsImage.setImageURI(result.uri)
            uploadImage(result.uri)
        }
    }

    private fun uploadImage(uri: Uri?) {

        mProgressDialog = ProgressDialog(this@SettingsActivity).apply {
            setTitle("Uploading Image...")
            setMessage("Please wait while we upload and process the image.")
            setCanceledOnTouchOutside(false)
            show()
        }
        val thumb_filePath = File(uri?.path)
        val current_user_id = mCurrentUser!!.uid
        val filepath = mImageStorage!!.child("profile_images").child(
            "$current_user_id.jpg"
        )
        val thumb_filepath =
            mImageStorage!!.child("profile_images").child("thumbs").child(
                "$current_user_id.jpg"
            )
        val ref = mImageStorage!!.child("profile_images").child("$current_user_id.jpg")
        uploadTask = uri?.let { ref.putFile(it) }!!

        val urlTask: Task<Uri> = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                mProgressDialog!!.cancel()
                Toast.makeText(this@SettingsActivity, "Exeption", Toast.LENGTH_SHORT).show()
                throw task.exception!!
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val downLoad_url = downloadUri.toString()
                Log.d("DOWNLOAD_URL","$downLoad_url")
                mUserDatabase?.child("image")?.setValue(downLoad_url)?.addOnCompleteListener { databaseTask ->
                    if (databaseTask.isSuccessful){
                        mProgressDialog?.dismiss()
                        Toast.makeText(this@SettingsActivity, "Success Uploading", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else {
                mProgressDialog!!.cancel()
            }
        }

    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}