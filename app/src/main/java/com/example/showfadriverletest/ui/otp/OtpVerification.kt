package com.example.showfadriverletest.ui.otp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.Constants
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.component.showSuccessAlert
import com.example.showfadriverletest.databinding.ActivityOtpVarifyBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.ui.home.MapsActivity
import com.example.showfadriverletest.ui.otp.viewmodel.OtpNavigator
import com.example.showfadriverletest.ui.otp.viewmodel.ResendOtpViewModel
import com.example.showfadriverletest.ui.register.registerprofile.RegisterProfile
import com.example.showfadriverletest.util.PopupDialog.showDialogOfLocationPolicy
import com.example.showfadriverletest.util.startNewActivity
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class OtpVerification : BaseActivity<ActivityOtpVarifyBinding, ResendOtpViewModel>(), OtpNavigator {
    override val layoutId: Int
        get() = R.layout.activity_otp_varify
    override val bindingVariable: Int
        get() = BR.viewModel

    var otp = ""
    var isTimer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, callback)
        timer.start()
        getOtpWithNumber()
        nextButtonClick()
    }

    private fun nextButtonClick() {
        binding.textviewLogin.setOnClickListener {
            if (validationOtp()) {
                if (Constants.Login == "Login") {
                    startNewActivity(MapsActivity::class.java, true)
                } else {
                    startNewActivity(RegisterProfile::class.java, true)
                }
            }
        }
    }

    private fun getOtpWithNumber() {
        binding.apply {
            dataStore.getOtp.asLiveData().observe(this@OtpVerification) {
                otp = it
                edtOtp.setText(otp)
            }

            dataStore.getMobileNo.asLiveData().observe(this@OtpVerification) {
                mViewModel.no = it
            }
        }
    }


    private var timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val hms = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                ),
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        millisUntilFinished
                    )
                )
            )
            binding.txtTimer.text = hms
        }

        override fun onFinish() {
            binding.textSignup.visibility = View.VISIBLE
            binding.txtTimer.visibility = View.GONE
        }
    }

   /* private fun startTimer() {
        object : CountDownTimer(60000, 1000) {
            // Callback function, fired on regular interval
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                if (isTimer) {
                    binding.textSignup.visibility = View.GONE
                    binding.txtTimer.visibility = View.VISIBLE
                    binding.txtTimer.text = "00:0" + millisUntilFinished / 1000

                } else {
                    binding.txtTimer.text = "00:0" + millisUntilFinished / 1000
                }
            }

            override fun onFinish() {
                binding.textSignup.visibility = View.VISIBLE
                binding.txtTimer.visibility = View.GONE
            }
        }.start()
    }*/

    override fun setUpObserver() {
        mViewModel.setNavigator(this)
        mViewModel.getResendOtpObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        showSuccessAlert(it.message.toString())
                        otp = it.data.otp.toString()
                        binding.edtOtp.setText(otp)
                        binding.textSignup.visibility = View.VISIBLE
                        binding.txtTimer.visibility = View.GONE
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    showDialogOfLocationPolicy(this@OtpVerification, it.message.toString())
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.e("LOADING-----------------", "LOADING::${it.status}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.please_check_internet_connection))
                }

                else -> {
                    showFailAlert(it.message!!)
                }
            }
        }
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            lifecycleScope.launch {
                Constants.Login = "LoginDenied"
                dataStore.setUserId("null")
            }
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            finish()
        }
    }

    override fun resend() {
        isTimer = true
        binding.textSignup.visibility = View.GONE
        binding.txtTimer.visibility = View.VISIBLE
        timer.start()

        mViewModel.callOtpResendApi()
    }

    private fun validationOtp(): Boolean {
        if (binding.edtOtp.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.please_enter_otp))
            return false
        } else if (binding.edtOtp.text.length < 6) {
            binding.root.showSnackBar(getString(R.string.please_enter_valid_otp))

            return false
        } else if (binding.edtOtp.text.toString() != otp) {
            binding.root.showSnackBar(getString(R.string.otp_doesn_t_match))
            return false
        }
        return true
    }


}