package com.example.showfadriverletest.ui.login

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.common.Constants
import com.example.showfadriverletest.common.Constants.flagPermission
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.component.showSuccessAlert
import com.example.showfadriverletest.databinding.ActivityLoginBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.login.LoginResponse
import com.example.showfadriverletest.ui.login.viewmodel.LoginNavigator
import com.example.showfadriverletest.ui.login.viewmodel.LoginViewModel
import com.example.showfadriverletest.ui.otp.OtpVerification
import com.example.showfadriverletest.ui.register.RegisterOtpActivity
import com.example.showfadriverletest.ui.termscondition.TermsOfService
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.PopupDialog.locationPolicyDialog
import com.example.showfadriverletest.util.SnackbarUtils
import com.example.showfadriverletest.util.startNewActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivitys : BaseActivity<ActivityLoginBinding, LoginViewModel>(), LoginNavigator {
    override val layoutId: Int
        get() = R.layout.activity_login
    override val bindingVariable: Int
        get() = BR.viewmodel

    var otp = ""
    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocationLat: Double? = null
    private var currentLocationLng: Double? = null
    private var isPostNotificationGranted = false
    private var flagLocationDenied = ""
    private var flagLocationStatus = false
    private var flagBackGroundPermission = false
    private val PERMISSION_REQUEST_CODE_BACKGROUND_PERMISSION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFun()
        locationPolicyDialoge()
        activityLauncher()
    /*      postNotification()*/
    }

    private fun locationPolicyDialoge() {
        locationPolicyDialog(
            this,
            getString(R.string.collects_location_data_to_enable_for_real_time_tracking_of_driver_even_when_the_app_is_in_background_or_not_in_use)
        ) {}
    }

    private fun initFun() {
        onBackPressedDispatcher.addCallback(this@LoginActivitys, callback)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@LoginActivitys)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isPostNotificationGranted = true
            requestPermissionLauncherBackground.launch(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            CommonFun.showSetting(
                this, getString(R.string.please_turn_on_postnotification_permission)
            )
        }
    }
    private val requestPermissionLauncherBackground = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            flagBackGroundPermission = true
            checkValidation()
        } else {
            CommonFun.showSetting(
                this,
                getString(R.string.allow_this_application_to_get_your_location_for_directions_and_background_data_nselection_is_only_the_following_n_n_allow_all_the_time)
            )
        }}

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
                finishAffinity()
            }
        }

        override fun setUpObserver() {
            mViewModel.setNavigator(this)
            mViewModel.getLoginObservable().observe(this) {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        myDialog.hide()
                        if (it.data?.status == true) {
                            showSuccessAlert(it.message!!)
                            /** To Store All data from Model to String*/
                            //   initBookingInfo = gson.toJson(it.data, LoginResponse::class.java)
                            /* initBookingInfo = gson.toJson(it.data)*/
                            lifecycleScope.launch {
                                dataStore.setDataStore(gson.toJson(it.data))
                            }
                            Log.e("LoginResponse save data", "setUpObserver:${it.data} ", )
                            getTokenId()
                            lifecycleScope.launch {
                                setLoginPreference(it)
                                Constants.Login = "Login"
                                startNewActivity(OtpVerification::class.java, true)
                                overridePendingTransition(
                                    R.anim.enter_from_right,
                                    R.anim.exit_to_left
                                )
                            }
                        } else {
                            /*  showFailAlert(it.message!!)*/
                            SnackbarUtils(
                                binding.root,
                                it.message!!,
                            ).snackieBar().show()
                            /*       binding.root.showSnackBar(it.message.toString())*/
                        }

                    }

                    Resource.Status.ERROR -> {
                        myDialog.hide()
                        SnackbarUtils(
                            binding.root,
                            it.message!!,
                        ).snackieBar().show()
                    }

                    Resource.Status.LOADING -> {
                        myDialog.show()
                    }

                    Resource.Status.NO_INTERNET_CONNECTION -> {
                        myDialog.hide()
                        SnackbarUtils(
                            binding.root,
                            getString(R.string.no_internet_connection),
                        ).snackieBar().show()
                        /* binding.root.showSnackBar(getString(R.string.no_internet_connection))*/
                    }

                    else -> {
                        showFailAlert(it.message!!)
                    }
                }

            }
        }

        private fun getTokenId() {
            dataStore.getFcmToken.asLiveData().observe(this@LoginActivitys) {
                deviceToken = it.toString()
                ApiConstant.token = it.toString()
            }
            dataStore.getUserId.asLiveData().observe(this@LoginActivitys) {
                ApiConstant.userid = it.toString()
            }
        }

        private suspend fun setLoginPreference(it: Resource<LoginResponse>) {
            otp = it.data?.otp.toString()
            var result = it.data?.data
            dataStore.apply {
                setUserId(result?.id.toString())
                setApiKey(result?.xApiKey.toString())
                setFirstName(result?.firstName.toString())
                setLastName(result?.lastName.toString())
                setMobileNo(result?.mobileNo.toString())
                setEmail(result?.email.toString())
                setGender(result?.gender.toString())
                setCarType(result?.carType.toString())
                setOwnerName(result?.ownerName.toString())
                setOwnerEmail(result?.ownerEmail.toString())
                setOwnerMobileNo(result?.ownerMobileNo.toString())
                setDob(result?.dob.toString())
                setProfileImage(result?.profileImage.toString())
                setUserId(result?.id.toString())
                setHasApiKey(result?.xApiKey.toString())
                setPlateNumber(result?.vehicleInfo?.get(0)!!.plateNumber.toString())
                setColor(result.vehicleInfo!![0]?.vehicleColor.toString())
                setLeftImage(result.vehicleInfo!![0]?.carLeft.toString())
                setRightImage(result.vehicleInfo!![0]?.carRight.toString())
                setFrontImage(result.vehicleInfo?.get(0)?.carFront.toString())
                setBackImage(result.vehicleInfo!![0]?.carBack.toString())
                setInsideImage(result.vehicleInfo!![0]?.carInterior.toString())
                setNationalIdNo(result.driverDocs?.nationalIdNumber.toString())
                setNationalIdImage(result.driverDocs?.nationalIdImage!!.toString())
                setDriverLicenceImage(result.driverDocs!!.driverLicenceImage!!.toString())
                setDriverLicenceExpiry(result.driverDocs!!.driverLicenceExpDate!!.toString())
                setNTSABudgeExpiry(result.driverDocs!!.psvBadgeExpDate!!.toString())
                setNtsaBadgeImage(result.driverDocs!!.psvBadgeImage!!.toString())
                setGoodConductImage(result.driverDocs!!.policeClearanceCerti!!.toString())
                setVehicleLogBookImage(result.driverDocs!!.vehicleLogBookImage!!.toString())
                setNtsaInspectionImage(result.driverDocs!!.ntsaInspectionImage!!.toString())
                setNTSAInspectionExpiry(result.driverDocs!!.ntsaExpDate!!.toString())
                setComprehensiveImage(result.driverDocs!!.psvComprehensiveInsurance!!.toString())
                setPsvComprehensiveExpiry(it.data?.data?.driverDocs?.psvComprehensiveInsuranceExpDate!!.toString())
                setOtp(otp)
                setMobileNo(binding.edtMobileNumber.text.toString())
            }
        }


        private fun checkValidation() {
            if (binding.edtMobileNumber.text.isEmpty()) {
                SnackbarUtils(
                    binding.root,
                    getString(R.string.empty_mobile_number),
                ).snackieBar().show()
            } else {
                callLoginApi()
            }
        }

        private fun callLoginApi() {
            mViewModel.userNumber = binding.edtMobileNumber.text.toString()
            mViewModel.callLoginApi(deviceToken, currentLocationLat, currentLocationLng)
        }

        private fun activityLauncher() {
            launcher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        /* flagLocationStatus = true*/
                        getCurrentLocation()
                    } else {
                        flagLocationStatus = false
                        flagLocationDenied = "deniedLocation"
                        CommonFun.showSetting(
                            this@LoginActivitys,
                            getString(R.string.please_turn_on_location_permission)
                        )
                    }
                }
        }

        private fun getCurrentLocation() {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PERMISSION_GRANTED
            ) {
                return
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    requestBackgroundPermission()
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location = task.result
                    Log.d("LOCATION", "location==>$location")
                    if (location != null) {
                        currentLocationLat = location.latitude
                        currentLocationLng = location.longitude
                        Log.d(TAG, "LAT---->${location.latitude}")
                        Log.d(TAG, "LNG---->${location.latitude}")
                        flagPermission = true
                    }
                }
            }
        }

        private fun requestBackgroundPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissionLauncherBackground.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                Toast.makeText(
                    this,
                    "Background location permission not allowed",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        override fun onLogin() {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        override fun onRegister() {
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            startNewActivity(RegisterOtpActivity::class.java, true)

        }

        fun termsOfService(view: View) {
            ApiConstant.ISCHECKED_TERM_OF_SERVICE = true
            ApiConstant.ISCHECKED_PRIVACY_POLICY = false
            startNewActivity(TermsOfService::class.java)
        }

        fun privacyPolicy(view: View) {
            ApiConstant.ISCHECKED_TERM_OF_SERVICE = false
            ApiConstant.ISCHECKED_PRIVACY_POLICY = true
            startNewActivity(TermsOfService::class.java)
        }

        fun register(view: View) {
            startNewActivity(RegisterOtpActivity::class.java, true)
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        }
    }
