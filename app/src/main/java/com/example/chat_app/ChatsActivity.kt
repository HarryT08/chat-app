package com.example.chat_app

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chat_app.adapter.UserAdapter
import com.example.chat_app.databinding.ActivityMainBinding
import com.example.chat_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsActivity : ComponentActivity() {
    var binding: ActivityMainBinding? = null
    var database: FirebaseDatabase? = null
    var user: User? = null
    var users:ArrayList<User>? = null
    var usersAdapter: UserAdapter? = null
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog = ProgressDialog(this@ChatsActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        usersAdapter = UserAdapter(this@ChatsActivity,users!!)
        val layoutManager = GridLayoutManager(this@ChatsActivity,1)
        binding!!.mRec.layoutManager = layoutManager
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        binding!!.mRec.adapter = usersAdapter
        database!!.reference.child("users").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for(snapshot1 in snapshot.children){
                    val user:User?= snapshot1.getValue(User::class.java)
                    if(!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                }
                usersAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    override fun onResume(){
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }
}