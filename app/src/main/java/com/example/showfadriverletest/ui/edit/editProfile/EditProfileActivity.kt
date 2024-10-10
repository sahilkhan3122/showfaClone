package com.example.showfadriverletest.ui.edit.editProfile

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.ActivityEditProfileBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.login.LoginResponse
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.CommonFun.checkIsConnectionReset
import com.example.showfadriverletest.util.PopupDialog
import com.example.showfadriverletest.util.SnackbarUtil
import com.example.showfadriverletest.util.setGlideImage
import com.example.showfadriverletest.view.showSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding, EditProfileViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_edit_profile
    override val bindingVariable: Int
        get() = BR.viewModel

    var tag = ""
    var spinnerData = ""
    var ownerName = ""
    var ownerEmail = ""
    var dob = ""
    var email = ""
    var number = ""
    var image: Bitmap? = null
    private var fName = ""
    private var lName = ""
    private var rdCarType = ""
    private var rdGender = ""
    private var selectOwnByPartner = ""
    private var imageEdit = ""
    private var imageUriCamera: Uri? = null
    private var ownerMobile = ""
    private var REQUEST_IMAGE_CAPTURE = 2
    var loginData: LoginResponse? = null


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[android.Manifest.permission.CAMERA] == true &&
                permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
            ) {
                openCamera()
            } else {
                CommonFun.showCameraSetting(
                    this,
                    getString(R.string.please_grant_camera_and_storage_permissions_in_the_app_settings)
                )
            }
        } else {
            if (permissions[android.Manifest.permission.CAMERA] == true &&
                permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true &&
                permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
            ) {
                openCamera()
            } else {
                CommonFun.showCameraSetting(
                    this,
                    getString(R.string.please_grant_camera_and_storage_permissions_in_the_app_settings)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backFun()
        setDataOnCreate()
        setSpinnerData()
        clickFun()
    }

    private fun setDataOnCreate() {
        lifecycleScope.launch {
            val job = lifecycleScope.async {
                //setPrefillData()
                dataStore.getDataStore.asLiveData().observe(this@EditProfileActivity) {
                    loginData = gson.fromJson(it, LoginResponse::class.java)
                    getData()
                    Log.e("setPrefillData--------------------------", "setPrefillData: $loginData")
                }
            }
            job.await()
        }
    }

    private fun getData() {
        binding.apply {
            if (loginData?.data != null) {
                loginData?.data?.apply {
                    etRegisterFirstname.setText(firstName)
                    etRegisterLastname.setText(lastName)
                    edtMobileNumber.setText(mobileNo)
                    edtEmail.setText(email)
                    etRegisterDob.setText(dob)
                    val img = ApiConstant.Image_URL.plus(profileImage)
                    setGlideImage(img, binding.ivProfileRegister, null)
                    checkGender(loginData?.data!!)
                    getCarType(loginData?.data!!)
                    setSpinnerData()
                }
            } else {
                /*    Toast.makeText(this@EditProfileActivity, "no Data Found", Toast.LENGTH_SHORT).show()*/
            }
        }
    }

    override fun setUpObserver() {
        mViewModel.getProfileInfoObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        /* ApiConstant.initBookingInfo = gson.toJson(it.data)*/
                        lifecycleScope.launch {
                            dataStore.setDataStore(gson.toJson(it.data))
                        }
                        Log.e(TAG, "on Profile Info success=>${it.message}")
                        PopupDialog.editVehicleDetailPopup(
                            this@EditProfileActivity, it.message.toString()
                        ) {
                            Handler(Looper.myLooper()!!).postDelayed({
                                finish()
                            }, 100)
                        }
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    lifecycleScope.launch {
                        if (!checkIsSessionnOut(it.code, getString(R.string.session_expire))) {
                            it.message?.let { message ->
                                if (checkIsConnectionReset(it.code)) {
                                    getString(R.string.no_internet)
                                } else {
                                    message
                                }
                            }
                        }
                    }
                    Log.e(TAG, "on Profile Info error=>${it.message}")
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.please_check_internet_connection))
                }

                Resource.Status.UNKNOWN -> {
                    myDialog.hide()
                    window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    if (it.code == 502) {
                        SnackbarUtil.show(
                            this, "no internet", Snackbar.LENGTH_LONG
                        )
                    } else {
                        SnackbarUtil.show(
                            this, "${it.message}", Snackbar.LENGTH_LONG
                        )
                    }
                }

                else -> {}
            }
        }
    }

    private fun backFun() {
        binding.activityHeader.tvTitle.text = getString(R.string.profile_detail)
        binding.activityHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
    }

    private fun clickFun() {
        binding.apply {
            tvRegisterProfileNext.setOnClickListener {
                checkValidation()
            }

            ivProfileRegister.setOnClickListener {
                checkPermissions()
            }
        }
    }

    private fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val storagePermission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storagePermission = ContextCompat.checkSelfPermission(
                this,
                arrayOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ).toString()
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            storagePermission = ContextCompat.checkSelfPermission(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (cameraPermission && storagePermission) {
            // Permissions are granted, open the camera
            openCamera()
        } else {
            // Request permissions
            requestPermissions()
        }
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else {
            /*below android 13 device */
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun openCamera() {

        val values = ContentValues(1)
        imageUriCamera =
            activity.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriCamera)
        intent.putExtra("reqCode", REQUEST_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val pathCol = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = imageUriCamera.let {
                activity.contentResolver.query(
                    it!!,
                    pathCol,
                    null,
                    null,
                    null
                )
            }
            cursor?.moveToFirst()
            val colIdx: Int = cursor?.getColumnIndex(pathCol[0])!!
            val img: String = cursor.getString(colIdx)
            Log.e("imgTest", "" + img)
            cursor.close()
            mViewModel.userProfile = imageUriCamera
            binding.ivProfileRegister.setImageURI(imageUriCamera)
        }
    }

    private fun getCarType(it: LoginResponse.Data) {
        binding.apply {
            if (it.carType == "own") {
                binding.carOwner.isChecked = true
                binding.llOwnerDetial.visibility = View.GONE
                rdCarType = "own"
                mViewModel.carType = rdCarType
            } else {
                binding.ownerByPartner.isChecked = true
                binding.llOwnerDetial.visibility = View.VISIBLE
                rdCarType = "rent"
                mViewModel.carType = rdCarType
                selectOwnByPartner = "partner"
                if (it.ownerName?.isNotEmpty() == true && it.ownerEmail?.isNotEmpty() == true && it.ownerMobileNo?.isNotEmpty() == true) {
                    etOwnerName.setText(it.ownerName)
                    etOwnerEmail.setText(it.ownerEmail)
                    etOwnerMobile.setText(it.ownerMobileNo)

                }
                /*dataStore.getAddress.asLiveData().observe(this@EditProfileActivity) {
                    setSpinnerData()
                }*/
            }
        }
    }

    private fun checkGender(it: LoginResponse.Data) {
        if (it.gender == "male") {
            binding.radioMale.isChecked = true
            rdGender = "male"
            mViewModel.gender = it.gender
        } else {
            binding.radioFemale.isChecked = true
            rdGender = "female"
            mViewModel.gender = it.gender.toString()
        }
    }

    private fun setSpinnerData() {
        val languages = resources.getStringArray(R.array.City)
        val spinner = binding.spArea
        val adapter = ArrayAdapter(this, R.layout.spinner_item, languages)
        spinner.adapter = adapter
        dataStore.getDataStore.asLiveData().observe(this@EditProfileActivity) {
            loginData = gson.fromJson(it, LoginResponse::class.java)
            spinner.setSelection(adapter.getPosition(loginData?.data?.address))
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {
                spinnerData = languages[position]
                mViewModel.address = spinnerData
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun checkValidation() {
        if (imageEdit == null) {
            binding.root.showSnackBar(getString(R.string.plz_select_profile_image))
        } else if (binding.etRegisterFirstname.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.plz_enter_first_name))
        } else if (binding.etRegisterLastname.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.plz_enter_last_name))
        } else if (!binding.ownerByPartner.isChecked && !binding.carOwner.isChecked) {
            binding.root.showSnackBar(getString(R.string.plz_select_radio))
        } else if (binding.edtEmail.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.please_enter_emaill))
        } else if (binding.edtMobileNumber.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.please_enter_mobile_number))
        } else if (tag == "rent") {
            if (binding.etOwnerName.text.isEmpty()) {
                binding.root.showSnackBar(getString(R.string.enter_owner_name))
            } else if (binding.etOwnerEmail.text.isEmpty()) {
                binding.root.showSnackBar(getString(R.string.enter_owner_email))
            } else if (binding.etOwnerMobile.text.isEmpty()) {
                binding.root.showSnackBar(getString(R.string.enter_owner_mobile_number))
            } else {
                tag = "cancel"
            }
        } else if (binding.etRegisterDob.text.isEmpty()) {
            binding.root.showSnackBar(getString(R.string.plz_select_birthdate))
        } else if (spinnerData == "SelectArea") {
            binding.root.showSnackBar(getString(R.string.plz_select_address_spinner))
        } else if (!binding.radioMale.isChecked && !binding.radioFemale.isChecked) {
            binding.root.showSnackBar(getString(R.string.plz_select_gender))
        } else {

            dataStore.getUserId.asLiveData().observe(this@EditProfileActivity) {
                mViewModel.id = it.toString()
            }
            fName = binding.etRegisterFirstname.text.toString()
            lName = binding.etRegisterLastname.text.toString()
            email = binding.edtEmail.text.toString()
            number = binding.edtMobileNumber.text.toString()
            dob = binding.etRegisterDob.text.toString()
            ownerName = binding.etOwnerName.text.toString()
            ownerMobile = binding.etOwnerMobile.text.toString()
            ownerEmail = binding.etOwnerEmail.text.toString()

            mViewModel.fname = fName
            mViewModel.lName = lName
            mViewModel.email = email
            mViewModel.number = number
            mViewModel.dob = dob
            mViewModel.ownname = ownerName
            mViewModel.ownMail = ownerEmail
            mViewModel.ownMobile = ownerMobile
            mViewModel.profileInfo()
        }
    }

    fun radioOwnerClick(view: View) {
        if (view.id == R.id.carOwner) {
            tag = "own"
            binding.llOwnerDetial.visibility = View.GONE
            rdCarType = "own"
            mViewModel.carType = rdCarType
        }

        if (view.id == R.id.ownerByPartner) {
            tag = "rent"
            binding.llOwnerDetial.visibility = View.VISIBLE
            rdCarType = "rent"
            mViewModel.carType = rdCarType


        }
    }

    fun radio_male_female_check(view: View) {
        if (view.id == R.id.radio_male) {
            rdGender = "male"
            mViewModel.gender = rdGender
        }

        if (view.id == R.id.radio_female) {
            rdGender = "female"
            mViewModel.gender = rdGender
        }
    }

    fun dateOfBirth(view: View) {
        CommonFun.hideKeyboard(this)
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.DatePickerTheme),
            {
                    _: DatePicker,
                    selectedYear: Int, selectedMonth: Int, selectedDay: Int,
                ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dob = dateFormat.format(selectedDate.time)
                binding.etRegisterDob.setText(dob)
            },
            year,
            month,
            day
        )
        datePickerDialog.setOnShowListener {
            val positiveButton = datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
        }
        datePickerDialog.datePicker.maxDate = Date().time
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "", datePickerDialog)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }
}