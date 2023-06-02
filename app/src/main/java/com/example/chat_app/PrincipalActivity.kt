package com.example.chat_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chat_app.databinding.ActivityPrincipalBinding
import com.example.chat_app.databinding.ActivityVerificationBinding

class PrincipalActivity : AppCompatActivity() {

    var binding : ActivityPrincipalBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding!!.btnRegistrar.setOnClickListener{
            val intent = Intent(this@PrincipalActivity,VerificationActivity::class.java)
            startActivity(intent)
        }
    }
}