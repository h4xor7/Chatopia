package com.panedey.chatopia.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.panedey.chatopia.databinding.MessageSingleLayoutBinding
import com.panedey.chatopia.models.Messages
import com.panedey.chatopia.utils.GetTimeAgo


class MessageAdapter(private val mMessageList: List<Messages>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var mUserDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
       // val v = LayoutInflater.from(parent.context).inflate(R.layout.message_single_layout, parent, false)
        val itemAddressBinding = MessageSingleLayoutBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return MessageViewHolder(itemAddressBinding)
    }

    override fun getItemCount(): Int {
        return mMessageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val c = mMessageList[position]
        val fromUser = c.from
        val messageType = c.type
        val time = c. time?.let { GetTimeAgo.getTimeAgo(it) }
        Log.d("TAG", "onBindViewHolder: $fromUser, $messageType")
        holder.messageSingleLayoutBinding.txtTime.text =  time.toString()
        Log.d("TAG", "onBindViewHolder: $time")

        if (fromUser != null) {
            mUserDatabase.child(fromUser).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)!!
                    val image = dataSnapshot.child("image").getValue(String::class.java)!!

                    //holder.messageSingleLayoutBinding.displayName.text = name
                    holder.messageSingleLayoutBinding.nameTextLayout.text = name

                   /* Picasso.with(holder.profileImage.context)
                        .load(image)
                        .placeholder(R.drawable.default_avatar)
                        .into(holder.profileImage)*/
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        if (messageType == "text") {
            holder.messageSingleLayoutBinding.messageTextLayout.text = c.message
            holder.messageSingleLayoutBinding.messageImageLayout.visibility = View.INVISIBLE
        } else {
            holder.messageSingleLayoutBinding.messageTextLayout .visibility = View.INVISIBLE
            /*Picasso.with(viewHolder.profileImage.context)
                .load(c.message)
                .placeholder(R.drawable.default_avatar)
                .into(viewHolder.messageImage)*/
        }

    }


    class MessageViewHolder( val messageSingleLayoutBinding: MessageSingleLayoutBinding) : RecyclerView.ViewHolder(messageSingleLayoutBinding.root) {

    }
}