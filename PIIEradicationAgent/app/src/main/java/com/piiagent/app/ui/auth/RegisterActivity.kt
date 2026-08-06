package com.piiagent.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.piiagent.app.databinding.ActivityRegisterBinding
import com.piiagent.app.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.txtGoLogin.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            if (!validate()) return@setOnClickListener

            // UI-only flow: no backend call. Proceeds directly into the app
            // and clears the auth back stack so Back doesn't return here.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun validate(): Boolean {
        val fullName = binding.tilFullName.editText?.text?.toString().orEmpty()
        val email = binding.tilEmail.editText?.text?.toString().orEmpty()
        val phone = binding.tilPhone.editText?.text?.toString().orEmpty()
        val password = binding.tilPassword.editText?.text?.toString().orEmpty()
        val confirmPassword = binding.tilConfirmPassword.editText?.text?.toString().orEmpty()

        binding.tilFullName.error = if (fullName.isBlank()) "Full name is required" else null
        binding.tilEmail.error = if (email.isBlank()) "Email is required" else null
        binding.tilPhone.error = if (phone.isBlank()) "Phone number is required" else null
        binding.tilPassword.error = if (password.isBlank()) "Password is required" else null
        binding.tilConfirmPassword.error = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }

        return listOf(
            binding.tilFullName.error,
            binding.tilEmail.error,
            binding.tilPhone.error,
            binding.tilPassword.error,
            binding.tilConfirmPassword.error
        ).all { it == null }
    }
}
