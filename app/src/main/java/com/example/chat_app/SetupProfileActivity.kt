package com.example.chat_app

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chat_app.databinding.ActivitySetupProfileBinding
import com.example.chat_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SetupProfileActivity : AppCompatActivity() {
    var binding:ActivitySetupProfileBinding? = null
    var auth:FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage : Uri? = null
    var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()
        binding!!.imageUser.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 45)
        }
        binding!!.profileContinueBtn.setOnClickListener {

            val name : String  = binding!!.profileName.text.toString()
            val lastname : String  = binding!!.profileLastname.text.toString()
            val username : String = binding!!.username.text.toString()
            val email : String = binding!!.email.text.toString()

            if(name.isEmpty()) {
                binding!!.profileName.setError("Porfavor ingresa un nombre")
            }

            if(lastname.isEmpty()) {
                binding!!.profileLastname.setError("Porfavor ingresa un apellido")
            }

            if(username.isEmpty()) {
                binding!!.username.setError("Porfavor ingresa un nombre de usuario")
            }

            if(email.isEmpty()) {
                binding!!.email.setError("Porfavor ingresa un email")
            }

            dialog!!.show()
            if(selectedImage != null){
                val reference = storage!!.reference.child("Profile").child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener {uri ->
                            val imageUrl = uri.toString()
                            val uid = auth!!.uid

                            val name : String  = binding!!.profileName.text.toString()
                            val lastname : String  = binding!!.profileLastname.text.toString()
                            val username : String = binding!!.username.text.toString()
                            val email : String = binding!!.email.text.toString()
                            val phoneNumber = auth!!.currentUser!!.phoneNumber

                            val user = User(uid,  name, username, lastname, phoneNumber, imageUrl, email)
                            database!!.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    dialog!!.dismiss()
                                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                    else {
                        val uid = auth!!.uid
                        val phoneNumber = auth!!.currentUser!!.phoneNumber
                        val name : String  = binding!!.profileName.text.toString()
                        val lastname : String  = binding!!.profileLastname.text.toString()
                        val username : String = binding!!.username.text.toString()
                        val email : String = binding!!.email.text.toString()

                        val user = User(uid, name, username, lastname, phoneNumber, "No image", email)
                        database!!.reference
                            .child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCanceledListener {
                                dialog!!.dismiss()
                                val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }


        }

    }
}