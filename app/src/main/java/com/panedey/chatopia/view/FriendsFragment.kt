package com.panedey.chatopia.view

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.panedey.chatopia.R
import com.panedey.chatopia.databinding.FragmentFriendsBinding
import com.panedey.chatopia.models.Friends
import com.panedey.chatopia.utils.Constants.FRND_REF
import com.panedey.chatopia.utils.Constants.USER_REF
import de.hdodenhof.circleimageview.CircleImageView


private var _binding: FragmentFriendsBinding? = null
private val binding get() = _binding!!
private lateinit var mAdapter: FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendHolder>


class FriendsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mFriendsDatabase: DatabaseReference? = null
    private var mUsersDatabase: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mCurrentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth!!.currentUser!!.uid

        mFriendsDatabase =
            FirebaseDatabase.getInstance().reference.child(FRND_REF).child(mCurrentUserId!!)
        mFriendsDatabase?.keepSynced(true)

        mUsersDatabase = FirebaseDatabase.getInstance().reference.child(USER_REF)
        mUsersDatabase?.keepSynced(true)

        binding.friendsList.hasFixedSize()
        binding.friendsList.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Friends>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(mFriendsDatabase!!, Friends::class.java)
            .build()


        mAdapter = object : FirebaseRecyclerAdapter<Friends, FriendHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_single_layout, parent, false)

                return FriendHolder(mView)
            }

            override fun onBindViewHolder(
                holder: FriendHolder,
                position: Int,
                model: Friends
            ) {
                val listUserId = getRef(position).key
                mUsersDatabase?.child(listUserId!!)
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userName = snapshot.child("name").value.toString()
                            val userThumb = snapshot.child("image").value.toString()
                            val userStatus = snapshot.child("status").value.toString()

                            if (snapshot.hasChild("online")) {
                                val userOnline: String = snapshot.child("online").value.toString()

                            }
                            holder.userText?.text = userName
                            holder.userStatus?.text = userStatus

                            holder.itemView.setOnClickListener {
                                val options = arrayOf<CharSequence>("Open Profile", "Send message")


                                val builder: AlertDialog.Builder = AlertDialog.Builder(context!!)

                                builder.setTitle("Select Options")
                                builder.setItems(options,
                                    DialogInterface.OnClickListener { dialogInterface, i -> //Click Event for each item.
                                        if (i == 0) {
                                            val profileIntent =
                                                Intent(context, ProfileActivity::class.java)
                                            profileIntent.putExtra("user_id", listUserId)
                                            startActivity(profileIntent)
                                        }
                                        if (i == 1) {
                                            val chatIntent =
                                                Intent(context, ChatActivity::class.java)
                                            chatIntent.putExtra("user_id", listUserId)
                                            chatIntent.putExtra("user_name", userName)
                                            startActivity(chatIntent)
                                        }
                                    })

                                builder.show()


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

        binding.friendsList.adapter = mAdapter
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


    class FriendHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val userText = itemView?.findViewById<TextView>(R.id.user_single_name)
        val userStatus = itemView?.findViewById<TextView>(R.id.user_single_status)
        val userImageView = itemView?.findViewById<CircleImageView>(R.id.user_single_image)
        val onLineImageView = itemView?.findViewById<ImageView>(R.id.user_single_online_icon)

    }


    companion object {

    }
}