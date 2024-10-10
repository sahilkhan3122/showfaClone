package com.example.showfadriverletest.ui.register

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.common.Constants
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.ActivityRegisterOtpBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.ui.login.LoginActivitys
import com.example.showfadriverletest.ui.otp.OtpVerification
import com.example.showfadriverletest.ui.register.registerotpviewmodel.RegisterOtpNavigator
import com.example.showfadriverletest.ui.register.registerotpviewmodel.RegisterOtpViewModel
import com.example.showfadriverletest.ui.termscondition.TermsOfService
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.PopupDialog
import com.example.showfadriverletest.util.PopupDialog.socketDailog
import com.example.showfadriverletest.util.startNewActivity
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RegisterOtpActivity : BaseActivity<ActivityRegisterOtpBinding, RegisterOtpViewModel>(),
    RegisterOtpNavigator {

    override val layoutId: Int
        get() = R.layout.activity_register_otp
    override val bindingVariable: Int
        get() = BR.viewModel
    private val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$".toRegex()
    var otp = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setUpObserver() {
        mViewModel.setNavigator(this)
        mViewModel.getRegisterOtpObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data!!.status) {
                        otp = it.data.otp.toString()
                        myDialog.hide()

                        PopupDialog.editVehicleDetailPopup(
                            this@RegisterOtpActivity,
                            it.message!!
                        ) {
                            lifecycleScope.launch {
                                dataStore.setOtp(otp)
                                Constants.Register = "Register"
                                startNewActivity(OtpVerification::class.java)
                                overridePendingTransition(
                                    R.anim.enter_from_right,
                                    R.anim.exit_to_left
                                )
                            }
                        }
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    if (it.code != 403) {
                        socketDailog(
                            this@RegisterOtpActivity, it.message!!
                        )
                    } else {
                        lifecycleScope.launch {
                            if (!checkIsSessionnOut(it.code, getString(R.string.session_expire))) {
                                it.message.let { message ->
                                    if (CommonFun.checkIsConnectionReset(it.code)) {
                                        getString(R.string.no_internet)
                                    } else {
                                        message
                                    }
                                }
                            }
                        }
                    }
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.please_check_internet_connection))
                }

                else -> {
                    binding.root.showSnackBar(it.message.toString())
                }
            }
        }
    }

    override fun termOfService() {
        ApiConstant.ISCHECKED_TERM_OF_SERVICE = true
        ApiConstant.ISCHECKED_PRIVACY_POLICY = false
        startNewActivity(TermsOfService::class.java)
    }

    override fun privacyPolicy() {
        ApiConstant.ISCHECKED_TERM_OF_SERVICE = false
        ApiConstant.ISCHECKED_PRIVACY_POLICY = true
        startNewActivity(TermsOfService::class.java)
    }

    override fun signIn() {
        startNewActivity(LoginActivitys::class.java)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }

    override fun next() {
        hideKeyboard()
        if (binding.edtEmail.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.please_enter_email))
        } else if (!isEmailValid(binding.edtEmail.text.trim().toString())) {
            binding.root.showSnackBar(getString(R.string.please_enter_valid_emaill))
        } else if (binding.edtMobileNumber.text!!.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.please_enter_mobile_no))
        } else if (binding.edtMobileNumber.text!!.length <= 8) {
            binding.root.showSnackBar(getString(R.string.invalid_mobile_number))
        } else {
            GlobalScope.launch {
                val em = binding.edtEmail.text.toString()
                val num = binding.edtMobileNumber.text.toString()
                dataStore.setEmail(em)
                dataStore.setMobileNo(num)
            }
            mViewModel.registerOtpApi()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return emailPattern.matches(email)
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }

}