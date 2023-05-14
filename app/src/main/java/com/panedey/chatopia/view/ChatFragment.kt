package com.panedey.chatopia.view

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.panedey.chatopia.R
import com.panedey.chatopia.databinding.FragmentChatBinding
import com.panedey.chatopia.models.Conv
import com.panedey.chatopia.utils.Constants
import com.panedey.chatopia.utils.Constants.DISPLAY_NAME_REF
import com.panedey.chatopia.utils.Constants.IMAGE_REF
import com.panedey.chatopia.utils.Constants.MSG_REF
import com.panedey.chatopia.utils.Constants.STATUS_REF
import com.panedey.chatopia.utils.Constants.TIME_STAMP_REF
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


private var _binding: FragmentChatBinding? = null
private val binding get() = _binding!!


class ChatFragment : Fragment() {
    private var mAuth: FirebaseAuth? = null

    private var mConvDatabase: DatabaseReference? = null
    private var mMessageDatabase: DatabaseReference? = null
    private var mUsersDatabase: DatabaseReference? = null
    private var mCurrentUserId: String? = null

    private lateinit var mAdapter: FirebaseRecyclerAdapter<Conv, ConvHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth?.currentUser?.uid
        mConvDatabase =
            FirebaseDatabase.getInstance().reference.child(Constants.CHAT_REF)
                .child(mCurrentUserId!!)
        mConvDatabase?.keepSynced(true)

        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(Constants.USER_REF)
        mMessageDatabase =
            FirebaseDatabase.getInstance().reference.child(Constants.MSG_REF)
                .child(mCurrentUserId!!)
        mUsersDatabase?.keepSynced(true)


        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true

        binding.convList.setHasFixedSize(true)
        binding.convList.layoutManager = linearLayoutManager


    }

    override fun onStart() {
        super.onStart()

        val conversationQuery: Query = mConvDatabase!!.orderByChild(TIME_STAMP_REF)

        val options = FirebaseRecyclerOptions.Builder<Conv>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(conversationQuery, Conv::class.java)
            .build()

        mAdapter = object : FirebaseRecyclerAdapter<Conv, ConvHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvHolder {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_single_layout, parent, false)

                return ConvHolder(mView)
            }

            override fun onBindViewHolder(
                holder: ConvHolder,
                position: Int,
                model: Conv
            ) {
                val listUserId = getRef(position).key


                mMessageDatabase?.child(listUserId!!)
                    ?.limitToLast(1)
                    ?.addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {
                            val data: String = snapshot.child(MSG_REF).value.toString()
                            holder.userStatus?.text = data
                            if (!model.seen) {
                                holder.userStatus?.setTypeface(
                                    holder.userStatus.typeface,
                                    Typeface.BOLD
                                )
                            } else {
                                holder.userStatus?.setTypeface(
                                    holder.userStatus.typeface,
                                    Typeface.NORMAL
                                )

                            }

                        }

                        override fun onChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onChildRemoved(snapshot: DataSnapshot) {

                        }

                        override fun onChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })


                mUsersDatabase?.child(listUserId!!)
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userName = snapshot.child(DISPLAY_NAME_REF).value.toString()
                            val userThumb = snapshot.child(IMAGE_REF).value.toString()
                            val userStatus = snapshot.child(STATUS_REF).value.toString()

                            if(userThumb!="default"){
                                Picasso.get().load(userThumb)
                                    .into(holder.userImageView)
                            }


                            if (snapshot.hasChild("online")) {
                                val userOnline: String = snapshot.child("online").value.toString()
                                if(userOnline == "true"){

                                    holder.onLineImageView?.visibility = View.VISIBLE;

                                }
                                else {

                                    holder.onLineImageView?.visibility = View.INVISIBLE;

                                }

                            }
                            holder.userText?.text = userName

                            holder.userStatus?.text = userStatus

                            holder.itemView.setOnClickListener {
                                val chatIntent = Intent(context, ChatActivity::class.java)
                                chatIntent.putExtra("user_id", listUserId)
                                chatIntent.putExtra("user_name", userName)
                                startActivity(chatIntent)

                            }



                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })




            }

            override fun onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
            }
        }
        binding.convList.adapter = mAdapter
        mAdapter.startListening()



    }
    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ConvHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val userText = itemView?.findViewById<TextView>(R.id.user_single_name)
        val userStatus = itemView?.findViewById<TextView>(R.id.user_single_status)
        val userImageView = itemView?.findViewById<CircleImageView>(R.id.user_single_image)
        val onLineImageView = itemView?.findViewById<ImageView>(R.id.user_single_online_icon)

    }

    companion object {
        private const val TAG = "ChatFragment"

    }
}