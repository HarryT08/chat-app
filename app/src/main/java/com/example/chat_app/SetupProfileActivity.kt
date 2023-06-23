package com.example.chat_app

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import com.example.chat_app.databinding.ActivitySetupProfileBinding
import com.example.chat_app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.HashMap

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
        dialog = ProgressDialog(this@SetupProfileActivity)
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

            if(selectedImage == null) {
                Toast.makeText(this@SetupProfileActivity, "Porfavor inserte una imagen de perfil", Toast.LENGTH_LONG).show()
                //binding!!.profileName.setError("Porfavor una imagen de perfil")
                return@setOnClickListener
            }

            if(name.isEmpty()) {
                binding!!.profileName.setError("Porfavor ingresa un nombre")
                return@setOnClickListener
            }

            if(lastname.isEmpty()) {
                binding!!.profileLastname.setError("Porfavor ingresa un apellido")
                return@setOnClickListener
            }

            if(username.isEmpty()) {
                binding!!.username.setError("Porfavor ingresa un nombre de usuario")
                return@setOnClickListener
            }

            if(email.isEmpty()) {
                binding!!.email.setError("Porfavor ingresa un email")
                return@setOnClickListener
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
                                    val intent = Intent(this@SetupProfileActivity, ChatsActivity::class.java)
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
                            .addOnCompleteListener {
                                dialog!!.dismiss()
                                val intent = Intent(this@SetupProfileActivity, ChatsActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data != null){
            if(data.data != null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString() + "")
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String,Any>()
                            obj["image"] = filePath
                            database!!.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnSuccessListener {  }
                        }
                    }

                }

                binding!!.imageUser.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}