package com.piiagent.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.piiagent.app.databinding.ActivityLoginBinding
import com.piiagent.app.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.tilEmail.editText?.text?.toString().orEmpty()
            val password = binding.tilPassword.editText?.text?.toString().orEmpty()

            if (!validate(email, password)) return@setOnClickListener

            // UI-only flow: no backend / auth call. Any valid-looking input proceeds
            // straight to the Dashboard using dummy data.
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.txtGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.txtForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset link sent (demo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isBlank()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        binding.tilEmail.error = null

        if (password.isBlank()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        binding.tilPassword.error = null
        return true
    }
}
