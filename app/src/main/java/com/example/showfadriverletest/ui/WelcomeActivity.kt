package com.example.showfadriverletest.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.showfadriverletest.R
import com.example.showfadriverletest.databinding.ActivityWelcomeBinding
import com.example.showfadriverletest.ui.login.LoginActivitys
import com.example.showfadriverletest.ui.register.RegisterOtpActivity
import com.example.showfadriverletest.util.startNewActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this@WelcomeActivity, callback)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
        clickFun()
    }

    private fun clickFun() {
        binding.tvLogin.setOnClickListener {
            startNewActivity(LoginActivitys::class.java)
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
        binding.tvRegister.setOnClickListener {
            startNewActivity(RegisterOtpActivity::class.java)
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            finishAffinity()
        }
    }
}