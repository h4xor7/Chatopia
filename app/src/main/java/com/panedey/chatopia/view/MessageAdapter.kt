package com.panedey.chatopia.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.panedey.chatopia.models.Messages
import com.panedey.chatopia.utils.GetTimeAgo


class MessageAdapter(private val mMessageList: List<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mUserDatabase: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("Users")
   private  var currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // val v = LayoutInflater.from(parent.context).inflate(R.layout.message_single_layout, parent, false)
        val view: View

        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                view = LayoutInflater.from(parent.context).inflate(com.panedey.chatopia.R.layout.item_message_sent, parent, false)
                SentMessageHolder(view)

            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(com.panedey.chatopia.R.layout.item_message_received, parent, false)
                 ReceivedMessageHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }



    }

    override fun getItemViewType(position: Int): Int {
        val message = mMessageList[position]
        return if (message.from == currentUserId) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mMessageList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    override fun getItemCount(): Int {
        return mMessageList.size
    }



/*
   inner class MessageViewHolder(val messageSingleLayoutBinding: MessageSingleLayoutBinding) :
        RecyclerView.ViewHolder(messageSingleLayoutBinding.root) {

    }
*/


    private inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private val messageText: TextView = itemView.findViewById(com.panedey.chatopia.R.id.text_gchat_message_me)
        private val timeText: TextView = itemView.findViewById(com.panedey.chatopia.R.id.text_gchat_timestamp_me)

        fun bind(message: Messages) {
            messageText.text = message.message
            timeText.text = message.time?.let { GetTimeAgo.getTimeAgo(it) }
        }
    }

    private inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        private val nameText: TextView = itemView.findViewById(com.panedey.chatopia.R.id.text_gchat_user_other)
        private val timeText: TextView = itemView.findViewById(com.panedey.chatopia.R.id.text_gchat_timestamp_other)
        private val messageText : TextView = itemView.findViewById(com.panedey.chatopia.R.id.text_gchat_message_other)
        private val profileImage: ImageView = itemView.findViewById(com.panedey.chatopia.R.id.image_gchat_profile_other)

        fun bind(message: Messages) {
            messageText.text = message.message
            timeText.text = message.time?.let { GetTimeAgo.getTimeAgo(it) }
            //nameText.text = message
            mUserDatabase.child(message.from!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)!!
                    val image = dataSnapshot.child("image").getValue(String::class.java)!!
                    //holder.messageSingleLayoutBinding.displayName.text = name
                    nameText.text = name

                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })


          //  Utils.displayRoundImageFromUrl(mContext, message.sender.profileUrl, profileImage)
        }
    }

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }




}