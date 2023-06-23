package com.example.chat_app

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chat_app.adapter.MessageAdapter
import com.example.chat_app.databinding.ActivityChatBinding
import com.example.chat_app.databinding.ActivityChatsBinding
import com.example.chat_app.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessageAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiveRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid:  String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Cargando imagen...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        //val name = intent.getStringExtra("name")

        binding!!.name.text = name
        Glide.with(this@ChatActivity)
            .load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.profile01)

        binding!!.imageView2.setOnClickListener{ finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference
            .child("Presence")
            .child(receiverUid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if(status == "offline"){
                            binding!!.status.visibility= View.GONE
                        }
                        else{
                            binding!!.status.setText(status)
                            binding!!.status.visibility= View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            } )

        senderRoom = senderUid + receiverUid
        receiveRoom = receiverUid + senderUid
        adapter = MessageAdapter(this@ChatActivity, messages, senderRoom!!, receiveRoom!!)

        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter = adapter
        database!!.reference
            .child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for( snapshot1 in snapshot.children ){
                        val message: Message? = snapshot1.getValue(Message::class.java)
                        message!!.mesaggeId = snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) { }

            })


        binding!!.send.setOnClickListener{

            val messageTxt: String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)

            binding!!.messageBox.setText("")
            val ramdonKey = database!!.reference
                .push().key

            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.mesagge!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference
                .child("chats")
                .child(senderRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference
                .child("chats")
                .child(receiveRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference
                .child("chats")
                .child(senderRoom!!)
                .child("message")
                .child(ramdonKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference
                        .child("chats")
                        .child(senderRoom!!)
                        .child("message")
                        .child(ramdonKey)
                        .setValue(message)
                        .addOnSuccessListener { }
                }

            database!!.reference
                .child("chats")
                .child(receiveRoom!!)
                .child("message")
                .child(ramdonKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference
                        .child("chats")
                        .child(receiveRoom!!)
                        .child("message")
                        .child(ramdonKey)
                        .setValue(message)
                        .addOnSuccessListener { }
                }
        }

        binding!!.attachment.setOnClickListener{

            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivity(intent)

        }

        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {

                database!!.reference
                    .child("Presence")
                    .child(senderUid!!)
                    .setValue("escribiendo...")

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)

            }

            var userStoppedTyping = Runnable {
                database!!.reference
                    .child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }

        })
        supportActionBar!!.setDisplayShowCustomEnabled(false)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == 25){
            if(data!!.data != null){
                val selectedImage = data.data
                val calendar = Calendar.getInstance()
                var reference = storage!!.reference
                    .child("chats")
                    .child(calendar.timeInMillis.toString()+"")
                dialog!!.show()

                reference.putFile(selectedImage!!)
                    .addOnCompleteListener{task ->
                        dialog!!.dismiss()
                        if(task.isSuccessful){
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageText: String = binding!!.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageText, senderUid, date.time)
                                message.mesagge = "photo"
                                message.imageUrl = filePath
                                binding!!.messageBox.setText("")
                                val ramdonKey = database!!.reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.mesagge!!
                                lastMsgObj["lastMsgTime"] = date.time

                                database!!.reference
                                    .child("chats")
                                    .child(senderRoom!!)
                                    .updateChildren(lastMsgObj)

                                database!!.reference
                                    .child("chats")
                                    .child(receiveRoom!!)
                                    .updateChildren(lastMsgObj)

                                database!!.reference
                                    .child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(ramdonKey!!)
                                    .setValue(message)
                                    .addOnSuccessListener {
                                        database!!.reference
                                            .child("chats")
                                            .child(receiveRoom!!)
                                            .child("messages")
                                            .child(ramdonKey)
                                            .setValue(message)
                                            .addOnSuccessListener {  }
                                    }
                            }
                        }
                    }
            }
        }

    }

    override fun onResume() {
        super.onResume()

        super.onPause()

        val currentId =  FirebaseAuth.getInstance().uid

        database!!.reference
            .child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()

        val currentId =  FirebaseAuth.getInstance().uid

        database!!.reference
            .child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }
}