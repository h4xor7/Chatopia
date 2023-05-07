package com.panedey.chatopia.view

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.panedey.chatopia.databinding.ActivityProfileBinding
import com.panedey.chatopia.utils.Constants.DISPLAY_NAME_REF
import com.panedey.chatopia.utils.Constants.FRND_REF
import com.panedey.chatopia.utils.Constants.FRND_RQ_REF
import com.panedey.chatopia.utils.Constants.IMAGE_REF
import com.panedey.chatopia.utils.Constants.NOTIFY_REF
import com.panedey.chatopia.utils.Constants.RQ_TYPE_REF
import com.panedey.chatopia.utils.Constants.STATE_FRND
import com.panedey.chatopia.utils.Constants.STATE_NOT_FRND
import com.panedey.chatopia.utils.Constants.STATE_RQ_RECIEVED
import com.panedey.chatopia.utils.Constants.STATE_RQ_SENT
import com.panedey.chatopia.utils.Constants.STATUS_REF
import com.panedey.chatopia.utils.Constants.USER_ID
import com.panedey.chatopia.utils.Constants.USER_REF
import java.text.DateFormat
import java.util.*


@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var mRootRef: DatabaseReference? = null
    private var mCurrentUser: FirebaseUser? = null
    private var mCurrentState: String? = null
    private var mNotificationDatabase: DatabaseReference? = null
    private var mFriendDatabase: DatabaseReference? = null
    private var mFriendReqDatabase: DatabaseReference? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mUsersDatabase: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initFun()

    }

    private fun initFun() {
        val userId = intent.getStringExtra(USER_ID)
        mRootRef = FirebaseDatabase.getInstance().reference
        mUsersDatabase = userId?.let {
            FirebaseDatabase.getInstance().reference.child(USER_REF).child(
                it
            )
        }

        mFriendReqDatabase = FirebaseDatabase.getInstance().reference.child(FRND_RQ_REF)
        mFriendDatabase = FirebaseDatabase.getInstance().reference.child(FRND_REF)
        mNotificationDatabase = FirebaseDatabase.getInstance().reference.child(NOTIFY_REF)
        mCurrentUser = FirebaseAuth.getInstance().currentUser


        mCurrentState = STATE_NOT_FRND
        binding.profileDeclineBtn.visibility = View.INVISIBLE
        binding.profileDeclineBtn.isEnabled = false

        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setTitle("Loading User Data")
        mProgressDialog!!.setMessage("Please wait while we load the user data.")
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.show()

        // Read from the database
        mUsersDatabase?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val displayName = dataSnapshot.child(DISPLAY_NAME_REF).value.toString()
                val status = dataSnapshot.child(STATUS_REF).value.toString()
                val image = dataSnapshot.child(IMAGE_REF).value.toString()

                binding.profileDisplayName.text = displayName
                binding.profileStatus.text = status
                // binding.profileImage.load(image)
                if (mCurrentUser?.uid.equals(userId)) {

                    // userprofile

                    binding.profileDeclineBtn.isEnabled = false
                    binding.profileDeclineBtn.visibility = View.INVISIBLE

                    binding.profileSendReqBtn.isEnabled = false
                    binding.profileSendReqBtn.visibility = View.INVISIBLE

                }


                //--------------- FRIENDS LIST / REQUEST FEATURE -----
                mCurrentUser?.uid?.let {
                    mFriendReqDatabase!!.child(it)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                mProgressDialog!!.dismiss()
                                Log.d(TAG, "onDataChange: It Means Not Own Profile")

                                if (snapshot.hasChild(userId!!)) {
                                   //path glt hai  requestType lene ka
                                    val reqType = snapshot.child(userId)
                                        .child("request_type").value.toString()
                                    Log.d(TAG, "onDataChange: $reqType")

                                    if (reqType == "received") {
                                        mCurrentState = STATE_RQ_RECIEVED
                                        binding.profileSendReqBtn.text = "Accept Friend Request"
                                        binding.profileDeclineBtn.visibility = View.VISIBLE
                                        binding.profileDeclineBtn.isEnabled = true
                                    }
                                    else if (reqType == "sent") {
                                        mCurrentState = STATE_RQ_SENT
                                        binding.profileSendReqBtn.text = "Cancel Friend Request"

                                        binding.profileDeclineBtn.visibility = View.INVISIBLE
                                        binding.profileDeclineBtn.isEnabled = false

                                    }
                                }
                                else {
                                    //mtlb friend hai
                                    mFriendDatabase!!.child(mCurrentUser!!.uid)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                mProgressDialog!!.dismiss()

                                                if (snapshot.hasChild(userId)) {
                                                    mCurrentState = STATE_FRND
                                                    binding.profileSendReqBtn.text =
                                                        "Unfriend this Person"

                                                    binding.profileDeclineBtn.visibility =
                                                        View.INVISIBLE
                                                    binding.profileDeclineBtn.isEnabled = false

                                                }

                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                mProgressDialog!!.dismiss()
                                            }

                                        })
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }


            }

            override fun onCancelled(error: DatabaseError) {
                mProgressDialog!!.dismiss();
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        binding.profileSendReqBtn.setOnClickListener {
            binding.profileSendReqBtn.isEnabled = false

            // --------------- NOT FRIENDS STATE ------------
            if (mCurrentState.equals(STATE_NOT_FRND)) {
                val mNotificationRef =
                    userId?.let { it1 -> mRootRef!!.child(NOTIFY_REF).child(it1).push() }
                val mNotificationId = mNotificationRef!!.key
                val notificationData: HashMap<String, String> = HashMap()
                notificationData["from"] = mCurrentUser!!.uid
                notificationData["type"] = "request"

                val requestMap = mapOf(
                    "Friend_req/${mCurrentUser!!.uid}/$userId/request_type" to "sent",
                    "Friend_req/$userId/${mCurrentUser!!.uid}/request_type" to "received",
                    "notifications/$userId/$mNotificationId" to notificationData
                )

                mRootRef!!.updateChildren(
                    requestMap
                ) { error, ref ->

                    if (error != null) {
                        Toast.makeText(
                            this,
                            "There was some error in sending request",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        mCurrentState = STATE_RQ_SENT;
                        binding.profileSendReqBtn.text = "Cancel Friend Request"

                    }

                    binding.profileSendReqBtn.isEnabled = true


                }

            }

            // - -------------- CANCEL REQUEST STATE ------------

            if (mCurrentUser!!.equals(STATE_RQ_SENT)) {
                if (userId != null) {
                    mFriendReqDatabase!!.child(mCurrentUser!!.uid).child(userId).removeValue()
                        .addOnSuccessListener {

                            mFriendReqDatabase!!.child(userId).child(mCurrentUser!!.uid)
                                .removeValue().addOnSuccessListener {
                                    binding.profileSendReqBtn.isEnabled = true
                                    mCurrentState = STATE_NOT_FRND
                                    binding.profileSendReqBtn.text = "Send Friend Request"
                                    binding.profileDeclineBtn.visibility = View.INVISIBLE
                                    binding.profileDeclineBtn.isEnabled = false

                                }
                        }
                }

            }


            // ------------ REQ RECEIVED STATE ----------

            if (mCurrentState.equals(STATE_RQ_RECIEVED)) {
                val currentDate: String = DateFormat.getDateTimeInstance().format(Date())

                val friendsMap = hashMapOf<String?, Any?>()
                friendsMap["Friends/${mCurrentUser!!.uid}/$userId/date"] = currentDate
                friendsMap["Friends/$userId/${mCurrentUser!!.uid}/date"] = currentDate
                friendsMap["Friend_req/${mCurrentUser!!.uid}/$userId"] = null
                friendsMap["Friend_req/$userId/${mCurrentUser!!.uid}"] = null

                mRootRef!!.updateChildren(
                    friendsMap
                ) { error, ref ->

                    if (error == null) {
                        binding.profileSendReqBtn.isEnabled = true
                        mCurrentState = STATE_FRND
                        binding.profileSendReqBtn.text = "Unfriend this Person"

                        binding.profileDeclineBtn.visibility = View.INVISIBLE
                        binding.profileDeclineBtn.isEnabled = false

                    } else {
                        val error = error.message
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()

                    }

                }


            }


            // ------------ UNFRIENDS ---------

            if (mCurrentState.equals(STATE_FRND)) {
                val unfriendMap = hashMapOf<String, Any?>()
                unfriendMap["Friends/${mCurrentUser!!.uid}/$userId"] = null
                unfriendMap["Friends/$userId/${mCurrentUser!!.uid}"] = null

                mRootRef!!.updateChildren(
                    unfriendMap
                ) { error, ref ->
                    if (error == null) {
                        mCurrentState = STATE_NOT_FRND
                        binding.profileSendReqBtn.text = "Send Friend Request"
                        binding.profileDeclineBtn.visibility = View.INVISIBLE
                        binding.profileDeclineBtn.isEnabled = false
                    } else {
                        val error: String = error.message
                        Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_SHORT).show()
                    }

                    binding.profileSendReqBtn.isEnabled = true

                }

            }





        }


    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}