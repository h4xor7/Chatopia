package com.panedey.chatopia.view

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.panedey.chatopia.databinding.ActivityChatBinding
import com.panedey.chatopia.models.Messages
import com.panedey.chatopia.utils.Constants.CHAT_REF
import com.panedey.chatopia.utils.Constants.IMAGE_REF
import com.panedey.chatopia.utils.Constants.MSG_REF
import com.panedey.chatopia.utils.Constants.SEEN_REF
import com.panedey.chatopia.utils.Constants.TIME_STAMP_REF
import com.panedey.chatopia.utils.Constants.USER_REF
import de.hdodenhof.circleimageview.CircleImageView


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private var mRootRef: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUserId: String? = null
    private val messagesList: MutableList<Messages> = ArrayList()
    private var itemPos = 0
    private var mChatUser: String? = null
    private var userName: String? = null
    private var mLastKey = ""
    private var mPrevKey = ""

    private lateinit var mTitleView: TextView
    lateinit var mLastSeenView: TextView
    private lateinit var mProfileImage: CircleImageView

    private val TOTAL_ITEMS_TO_LOAD = 10
    private var mCurrentPage = 1

    private val GALLERY_PICK = 1
    private val mLinearLayout: LinearLayoutManager? = null
    private var mAdapter: MessageAdapter? = null

    private var mImageStorage: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.topAppBar as Toolbar)
        //supportActionBar?.title = "All Users"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.setDisplayShowCustomEnabled(true)

        mRootRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth!!.currentUser!!.uid

        mChatUser = intent.getStringExtra("user_id")
        userName = intent.getStringExtra("user_name")

        //val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
       // val actionBarView = inflater.inflate(R.layout.chat_custom_bar, null)
      //  actionBar?.customView = actionBarView

      //  mTitleView = findViewById<TextView>(R.id.custom_bar_title)
      //  mLastSeenView = findViewById<TextView>(R.id.custom_bar_seen)
       // mProfileImage = findViewById<CircleImageView>(R.id.custom_bar_image)

        bindHandlers()


    }

    private fun bindHandlers() {
        mRootRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth?.currentUser?.uid
        binding.messagesList.hasFixedSize()
        binding.messagesList.layoutManager = LinearLayoutManager(this)
        mAdapter = MessageAdapter(messagesList)
        binding.messagesList.adapter = mAdapter

        //image storage
        mImageStorage = FirebaseStorage.getInstance().reference
        mRootRef?.child(CHAT_REF)?.child(mCurrentUserId!!)?.child(mChatUser!!)?.child(SEEN_REF)
            ?.setValue(true)
        loadMessage()

        binding.customBarTitle.text = userName


        mRootRef?.child(USER_REF)?.child(mChatUser!!)?.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val online = "true"
                val image: String = snapshot.child(IMAGE_REF).value.toString()
                val onlineValue = snapshot.child(SEEN_REF).value.toString()

                Log.d(Companion.TAG, "onDataChange:$onlineValue ")
               /* if (online == "true")
                {
                    binding.customBarSeen.text = "Online"

                }
                else {
                    val getTimeAgo = GetTimeAgo()
                    val lastTime = online.toLong()
                    val lastSeenTime: String = getTimeAgo(lastTime)!!
                    binding.customBarSeen.text = lastSeenTime


                }*/
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })



        mRootRef?.child(CHAT_REF)?.child(mCurrentUserId!!)
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(mChatUser!!)) {

                        val chatAddMap = HashMap<String, Any>()
                        chatAddMap["seen"] = false
                        chatAddMap["timestamp"] = ServerValue.TIMESTAMP

                        val chatUserMap = HashMap<String, Any>()
                        chatUserMap["Chat/$mCurrentUserId/$mChatUser"] = chatAddMap
                        chatUserMap["Chat/$mChatUser/$mCurrentUserId"] = chatAddMap

                        mRootRef?.updateChildren(chatUserMap,
                            object : DatabaseReference.CompletionListener {
                                override fun onComplete(
                                    error: DatabaseError?,
                                    ref: DatabaseReference
                                ) {

                                    if (error != null) {

                                        Log.d("CHAT_LOG", error.message.toString())

                                    }
                                }

                            })


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO("Not yet implemented")
                }

            })


        binding.chatSendBtn.setOnClickListener {
            sendMessage()
        }


        binding.chatAddBtn.setOnClickListener {

        }

        binding.messageSwipeLayout.setOnRefreshListener {
            mCurrentPage++

            itemPos = 0

            loadMoreMessages()
        }


    }

    private fun loadMoreMessages() {
        val messageRef = mRootRef!!.child(MSG_REF).child(mCurrentUserId!!).child(
            mChatUser!!
        )
        val messageQuery: Query = messageRef.orderByKey().endAt(mLastKey).limitToLast(10)
        messageQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Messages::class.java)
                val messageKey = snapshot.key

                if (mPrevKey != messageKey) {
                    messagesList.add(itemPos++, message!!)


                } else {
                    mPrevKey = mLastKey
                }

                if (itemPos == 1) {
                    mLastKey = messageKey!!
                }

                Log.d(
                    "TOTALKEYS",
                    "Last Key : $mLastKey | Prev Key : $mPrevKey | Message Key : $messageKey"
                )

                mAdapter?.notifyDataSetChanged()
                binding.messageSwipeLayout.isRefreshing = false
                mLinearLayout?.scrollToPositionWithOffset(10, 0)


            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun sendMessage() {
        val message: String = binding.chatMessageView.text.toString()
        if (!TextUtils.isEmpty(message)) {
            val currentUserRef = "$MSG_REF/$mCurrentUserId/$mChatUser"
            val chatUserRef = "$MSG_REF/$mChatUser/$mCurrentUserId"

            val userMessagePush = mRootRef!!.child(MSG_REF)
                .child(mCurrentUserId!!).child(mChatUser!!).push()
            val pushId: String = userMessagePush.key!!


            val messageMap = hashMapOf<String, Any>(
                "message" to message,
                "seen" to false,
                "type" to "text",
                "time" to ServerValue.TIMESTAMP,
                "from" to mCurrentUserId!!
            )

            val messageUserMap = hashMapOf<String, Any>(
                "$currentUserRef/$pushId" to messageMap,
                "$chatUserRef/$pushId" to messageMap
            )

            binding.chatMessageView.setText("")


            mRootRef!!.child(CHAT_REF).child(mCurrentUserId!!).child(mChatUser!!).child(SEEN_REF)
                .setValue(true);
            mRootRef!!.child(CHAT_REF).child(mCurrentUserId!!).child(mChatUser!!)
                .child(TIME_STAMP_REF).setValue(ServerValue.TIMESTAMP)

            mRootRef!!.child(CHAT_REF).child(mChatUser!!).child(mCurrentUserId!!).child(SEEN_REF)
                .setValue(false);
            mRootRef!!.child(CHAT_REF).child(mChatUser!!).child(mCurrentUserId!!)
                .child(TIME_STAMP_REF).setValue(ServerValue.TIMESTAMP)

            mRootRef!!.updateChildren(
                messageUserMap,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.message.toString())
                    }
                })

        }
    }

    private fun loadMessage() {
        val messageRef = mRootRef!!.child(MSG_REF).child(mCurrentUserId!!).child(mChatUser!!)

        val messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD)

        messageQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Messages::class.java)

                itemPos++

                if (itemPos == 1) {
                    val messageKey = snapshot.key

                    if (messageKey != null) {
                        mLastKey = messageKey
                        mPrevKey = messageKey

                    }
                }

                messagesList.add(message!!)
                mAdapter?.notifyDataSetChanged()

                binding.messagesList.scrollToPosition(messagesList.size - 1)

                binding.messageSwipeLayout.isRefreshing = false

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    companion object {
        private const val TAG = "ChatActivity"
    }
}