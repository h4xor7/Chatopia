package com.panedey.chatopia.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.panedey.chatopia.R
import com.panedey.chatopia.databinding.ActivityUsersBinding
import com.panedey.chatopia.models.Users
import com.panedey.chatopia.utils.Constants
import com.panedey.chatopia.utils.Constants.USER_REF
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class UsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var mDatabase: DatabaseReference

    private lateinit var mLayoutManager: LayoutManager
    private lateinit var mAdapter: FirebaseRecyclerAdapter<Users, UserHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.usersAppBar as Toolbar)
        supportActionBar?.title = "All Users"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDatabase = FirebaseDatabase.getInstance().reference.child(USER_REF)

        mLayoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(binding.usersList.context, VERTICAL)
        binding.usersList.addItemDecoration(dividerItemDecoration)
        binding.usersList.setHasFixedSize(true)
        binding.usersList.layoutManager = mLayoutManager







    }

    override fun onStart() {
        super.onStart()

        val options = FirebaseRecyclerOptions.Builder<Users>()
            .setLifecycleOwner(this)
            .setQuery(mDatabase, Users::class.java)
            .build()


        mAdapter = object : FirebaseRecyclerAdapter<Users, UserHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
                val mView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.users_single_layout, parent, false)

                return UserHolder(mView)
            }

            override fun onBindViewHolder(
                holder: UserHolder,
                position: Int,
                model: Users
            ) {
                holder.userText?.text = model.name.toString()
                holder.userStatus?.text = model.status.toString()
                val userId = getRef(position).key


                Picasso.get().load(model.image.toString())
                    .placeholder(resources.getDrawable(R.drawable.default_avatar))
                    .into( holder.userImageView)

                /*  holder.userImageView?.load(model.image.toString()){
                      placeholder(R.drawable.default_avatar)
                  }*/

                holder.itemView.setOnClickListener {
                    val profileIntent = Intent(this@UsersActivity, ProfileActivity::class.java)
                    profileIntent.putExtra(Constants.USER_ID, userId)
                    startActivity(profileIntent)
                }


            }

            override fun onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                //mEmptyListMessage.setVisibility(if (itemCount == 0) View.VISIBLE else View.GONE)
            }
        }

        binding.usersList.adapter = mAdapter

        mAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    class UserHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val userText = itemView?.findViewById<TextView>(R.id.user_single_name)
        val userStatus = itemView?.findViewById<TextView>(R.id.user_single_status)
        val userImageView = itemView?.findViewById<CircleImageView>(R.id.user_single_image)


    }
}

