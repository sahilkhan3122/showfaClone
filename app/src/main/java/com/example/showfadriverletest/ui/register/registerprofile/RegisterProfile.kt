package com.example.showfadriverletest.ui.register.registerprofile

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.databinding.ActivityRegisterProfileBinding
import com.example.showfadriverletest.ui.register.registerprofile.viewmodel.RegisterProfileModel
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class RegisterProfile : BaseActivity<ActivityRegisterProfileBinding, RegisterProfileModel>() {
    override val layoutId: Int
        get() = R.layout.activity_register_profile
    override val bindingVariable: Int
        get() = BR.viewModel

    var tag = ""
    private var fName = ""
    private var lName = ""
    private var rdCarType = ""
    private var rdGender = ""
    var dob = ""
    var spinnerData = ""
    var image: Bitmap? = null
    private var imageUriCamera: Uri? = null
    private var REQUEST_IMAGE_CAPTURE = 2
    private val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$".toRegex()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[android.Manifest.permission.CAMERA] == true
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
        binding.apply {
            defaultSelection()
            activityHeader.tvTitle.text = getString(R.string.profile_detail)
            activityHeader.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            }
            setSpinnerData()
            tvRegisterProfileNext.setOnClickListener {
                checkValidation()
            }

            llRegisterUserProfile.setOnClickListener {
                requestPermissions()
            }
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
            // binding.img.setImageURI(Uri.fromFile(File(img)))
            binding.ivProfileRegister.setImageURI(imageUriCamera)
            lifecycleScope.launch {
                dataStore.setProfileImage(imageUriCamera.toString())
                Log.e("imgTest", "" + img)
            }
        }
    }


    private fun openCamera() {
        val values = ContentValues()
        imageUriCamera = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
      /*  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)*/
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriCamera)
       /* if (intent.resolveActivity(packageManager) != null) {*/
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
       /* }*/
    }



    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA)
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


    private fun defaultSelection() {
        binding.carOwner.isChecked = true
        binding.radioMale.isChecked = true
        rdGender = "male"
        rdCarType = "own"
    }

    override fun setUpObserver() {
    }

    private fun setSpinnerData() {
        val languages = resources.getStringArray(R.array.City)
        val spinner = binding.spArea
        val adapter = ArrayAdapter(
            this, R.layout.spinner_item, languages
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {
                spinnerData = languages[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }
    private fun isEmailValid(email: String): Boolean {
        return emailPattern.matches(email)
    }


    private fun checkValidation() {
        when {
            imageUriCamera == null -> {
                binding.root.showSnackBar(getString(R.string.plz_select_profile_image))
            }
            binding.etRegisterFirstname.text.isEmpty() -> {
                binding.root.showSnackBar(getString(R.string.plz_enter_first_name))
            }
            binding.etRegisterLastname.text.isEmpty() -> {
                binding.root.showSnackBar(getString(R.string.plz_enter_last_name))
            }
            !binding.ownerByPartner.isChecked && !binding.carOwner.isChecked -> {
                binding.root.showSnackBar(getString(R.string.plz_select_radio))
            }
            tag == "rent" -> {
                if(binding.etOwnerName.text.isEmpty()){
                    binding.root.showSnackBar(getString(R.string.enter_owner_name))
                }else if(binding.etOwnerMobile.text.isEmpty()){
                    binding.root.showSnackBar(getString(R.string.enter_owner_mobile_number))
                }else if(binding.etOwnerEmail.text.isEmpty()){
                    binding.root.showSnackBar(getString(R.string.enter_owner_email))
                }
                else if (!isEmailValid(binding.etOwnerEmail.text.trim().toString())) {
                    binding.root.showSnackBar(getString(R.string.please_enter_valid_owner_email))
                }else{
                    tag = "cancel"
                    lifecycleScope.launch {
                        dataStore.setOwnerName(binding.etOwnerName.text.toString())
                        dataStore.setOwnerEmail(binding.etOwnerEmail.text.toString())
                        dataStore.setOwnerMobileNo(binding.etOwnerMobile.text.toString())
                    }
                }
            }
            binding.etRegisterDob.text.isEmpty() -> {
                binding.root.showSnackBar(getString(R.string.plz_select_birthdate))
            }
            spinnerData == "SelectArea" -> {
                binding.root.showSnackBar(getString(R.string.plz_select_address_spinner))
            }
            !binding.radioMale.isChecked && !binding.radioFemale.isChecked -> {
                binding.root.showSnackBar(getString(R.string.plz_select_gender))
            }
            else -> {
                myDialog.show()
                fName = binding.etRegisterFirstname.text.toString()
                lName = binding.etRegisterLastname.text.toString()

                lifecycleScope.launch {
                    dataStore.setFirstName(fName)
                    dataStore.setLastName(lName)
                    dataStore.setDob(dob)
                    dataStore.setCarType(rdCarType)
                    dataStore.setGender(rdGender)
                    dataStore.setAdress(spinnerData)
                    dataStore.setProfileImage(imageUriCamera.toString())
                }
                myDialog.hide()
                startActivity(Intent(this, VehicleDetail::class.java))
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
            }
        }
    }

    fun radioOwnerClick(view: View) {
        if (view.id == R.id.carOwner) {
            binding.llOwnerDetial.visibility = View.GONE
            rdCarType = "own"
            tag = "cancel"
        }

        if (view.id == R.id.ownerByPartner) {
            binding.llOwnerDetial.visibility = View.VISIBLE
            rdCarType = "rent"
            tag = "rent"
        }
    }

    fun radioGender(view: View) {
        if (view.id == R.id.radio_male) {
            rdGender = "male"
        }
        if (view.id == R.id.radio_female) {
            rdGender = "female"
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
        // Hide the default button text
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "", datePickerDialog)
        datePickerDialog.setCancelable(false)
        datePickerDialog.show()
    }
}