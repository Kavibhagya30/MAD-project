package com.piiagent.app.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.piiagent.app.databinding.ActivitySplashBinding
// NOTE: LoginActivity is generated in the next part of this project.
// Once ui.auth.LoginActivity exists, this import + intent will resolve.
import com.piiagent.app.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    companion object {
        private const val SPLASH_DELAY_MS = 2200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playEntryAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, SPLASH_DELAY_MS)
    }

    private fun playEntryAnimation() {
        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 0.7f
        binding.imgLogo.scaleY = 0.7f
        binding.txtAppName.alpha = 0f
        binding.txtTagline.alpha = 0f

        val logoFade = ObjectAnimator.ofFloat(binding.imgLogo, "alpha", 0f, 1f)
        val logoScaleX = ObjectAnimator.ofFloat(binding.imgLogo, "scaleX", 0.7f, 1f)
        val logoScaleY = ObjectAnimator.ofFloat(binding.imgLogo, "scaleY", 0.7f, 1f)
        val nameFade = ObjectAnimator.ofFloat(binding.txtAppName, "alpha", 0f, 1f).apply {
            startDelay = 250
        }
        val taglineFade = ObjectAnimator.ofFloat(binding.txtTagline, "alpha", 0f, 1f).apply {
            startDelay = 400
        }

        AnimatorSet().apply {
            duration = 650
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(logoFade, logoScaleX, logoScaleY, nameFade, taglineFade)
            start()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
