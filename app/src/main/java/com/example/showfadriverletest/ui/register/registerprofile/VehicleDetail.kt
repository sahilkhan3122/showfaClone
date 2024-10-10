package com.example.showfadriverletest.ui.register.registerprofile

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.Constants.CAR_BACK_IMAGE
import com.example.showfadriverletest.common.Constants.CAR_BACK_IMAGE_CODE
import com.example.showfadriverletest.common.Constants.CAR_FRONT_IMAGE
import com.example.showfadriverletest.common.Constants.CAR_FRONT_IMAGE_CODE
import com.example.showfadriverletest.common.Constants.CAR_INSIDE_IMAGE
import com.example.showfadriverletest.common.Constants.CAR_INSIDE_IMAGE_CODE
import com.example.showfadriverletest.common.Constants.CAR_LEFT_IMAGE
import com.example.showfadriverletest.common.Constants.CAR_LEFT_IMAGE_CODE
import com.example.showfadriverletest.common.Constants.CAR_RIGHT_IMAGE
import com.example.showfadriverletest.common.Constants.CAR_RIGHT_IMAGE_CODE
import com.example.showfadriverletest.common.Constants.selectBackImage
import com.example.showfadriverletest.common.Constants.selectFrontImage
import com.example.showfadriverletest.common.Constants.selectInsideImage
import com.example.showfadriverletest.common.Constants.selectLeftImage
import com.example.showfadriverletest.common.Constants.selectRightImage
import com.example.showfadriverletest.common.Constants.selectVehicleManufactuer
import com.example.showfadriverletest.common.Constants.selectVehicleModel
import com.example.showfadriverletest.common.Constants.selectVehicleYear
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.ActivityVehicleDetailBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.vehicledetail.VehicleManufacturerResponse
import com.example.showfadriverletest.ui.register.registerprofile.viewmodel.VehicleDetailViewModel
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.startNewActivity
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class VehicleDetail : BaseActivity<ActivityVehicleDetailBinding, VehicleDetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_vehicle_detail
    override val bindingVariable: Int
        get() = BR.viewModel

    var selectCarPos = 0
    var vehicleYear = ""
    var flag = ""
    private var manufactureModelList: MutableList<String> = ArrayList()
    var vehicleModelList: ArrayList<String> = ArrayList()
    var manufactureYearList: MutableList<String> = ArrayList()
    private var modelList: MutableList<VehicleManufacturerResponse.DataItem> = ArrayList()
    private var manufacturerNameAdapter: ArrayAdapter<*>? = null
    private var vehicleModelAdapter: ArrayAdapter<*>? = null
    private var manufactureYearAdapter: ArrayAdapter<*>? = null
    private var imageUri: Uri? = null
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private val CAMERA_PERMISSION_CODE = 1000
    private val STORAGE_PERMISSION_CODE = 2
    private var IMAGE_CAPTURE_CODE = 0
    var manufactureName = ""
    var vehicleTypeModelNameModel = ""
    var vehicleManufacureId = ""
    var vehicleModelId = ""
    var vehicleType = ""


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions[android.Manifest.permission.CAMERA] == true
            ) {
                openCameraInterface()
            } else {
                CommonFun.showCameraSetting(
                    this,
                    getString(R.string.please_grant_camera_and_storage_permissions_in_the_app_settings)
                )
            }
        } else {
            if (permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            ) {
                openCameraInterface()
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
        mViewModel.setNavigator(this@VehicleDetail)
        mViewModel.vehicleManufactureDetailApi()
        /*  selectVehicleModel = true
          selectVehicleYear = true
          selectVehicleManufactuer = true*/
        binding.activityHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }

        cameraResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == RESULT_OK) {
                    when (IMAGE_CAPTURE_CODE) {
                        CAR_RIGHT_IMAGE_CODE -> {
                            flag = CAR_RIGHT_IMAGE
                            mViewModel.docFile = imageUri
                            mViewModel.uploadDocsApi()
                            binding.ivCarImageRight.setImageURI(imageUri)
                            lifecycleScope.launch {
                                dataStore.setRightImage(imageUri.toString())
                            }
                        }

                        CAR_LEFT_IMAGE_CODE -> {
                            flag = CAR_LEFT_IMAGE
                            mViewModel.docFile = imageUri
                            mViewModel.uploadDocsApi()
                            binding.ivCarImageLeft.setImageURI(imageUri)
                        }

                        CAR_FRONT_IMAGE_CODE -> {
                            flag = CAR_FRONT_IMAGE
                            mViewModel.docFile = imageUri
                            mViewModel.uploadDocsApi()
                            binding.ivCarImageFront.setImageURI(imageUri)
                        }

                        CAR_BACK_IMAGE_CODE -> {
                            flag = CAR_BACK_IMAGE
                            mViewModel.docFile = imageUri
                            mViewModel.uploadDocsApi()
                            binding.ivCarImageBack.setImageURI(imageUri)
                        }

                        CAR_INSIDE_IMAGE_CODE -> {
                            flag = CAR_INSIDE_IMAGE
                            mViewModel.docFile = imageUri
                            mViewModel.uploadDocsApi()
                            binding.ivCarImageInside.setImageURI(imageUri)
                        }

                        else -> {
                        }
                    }
                }
            }

        binding.apply {
            activityHeader.tvTitle.text = getString(R.string.vehicle_detaill)
            tvRegisterVehicleNext.setOnClickListener {
                if (etVehiPlateNo.text.isEmpty()) {
                    binding.root.showSnackBar(getString(R.string.please_enter_vehicle_plate_number))
                } else if (selectVehicleManufactuer) {
                    binding.root.showSnackBar(getString(R.string.please_enter_vehicle_manufacture_spinner))
                } else if (selectVehicleModel) {
                    binding.root.showSnackBar(getString(R.string.please_enter_vehicle_model_spinner))
                } else if (selectVehicleYear) {
                    binding.root.showSnackBar(getString(R.string.please_select_manufacture_year))
                } else if (etVehicleColor.text.isEmpty()) {
                    binding.root.showSnackBar(getString(R.string.please_enter_vehicle_color_spinner))
                } else if (!selectRightImage) {
                    binding.root.showSnackBar(getString(R.string.please_enter_right_image))
                } else if (!selectLeftImage) {
                    binding.root.showSnackBar(getString(R.string.please_select_left_image))
                } else if (!selectFrontImage) {
                    binding.root.showSnackBar(getString(R.string.please_enter_front_image))
                } else if (!selectBackImage) {
                    binding.root.showSnackBar(getString(R.string.please_enter_back_image))
                } else if (!selectInsideImage) {
                    binding.root.showSnackBar(getString(R.string.please_enter_inside_image))
                } else {
                    lifecycleScope.launch {
                        dataStore.setPlateNumber(binding.etVehiPlateNo.text.toString())
                        dataStore.setColor(binding.etVehicleColor.text.toString())
                    }
                    startNewActivity(DocumentDetail::class.java, false)
                }
            }

        }

        vehicleModelAdapter =
            ArrayAdapter(activity, android.R.layout.simple_spinner_item, vehicleModelList)
        vehicleModelAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVehicleModel.adapter = vehicleModelAdapter
    }


    override fun setUpObserver() {
        mViewModel.getManufactureVehicleObservable().observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    resource.data!!.let {
                        if (it.status) {
                            myDialog.hide()
                            setVehicleManufacturerData(it)
                        } else {
                            showFailAlert(it.message)
                        }
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "on error----------------=>${resource.message}")
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${resource.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "no internet=>${resource.message}")
                    isInternetConnected = false/*   showInternetDialogForSplash(false)*/
                }

                else -> {}
            }
        }

        mViewModel.getUploadDocsObservable().observe(this) { resource ->
            when (resource.status) {
                Resource.Status.SUCCESS -> {
                    resource.data!!.let {
                        if (it.status) {
                            myDialog.hide()
                            when (flag) {
                                CAR_LEFT_IMAGE -> {
                                    lifecycleScope.launch {
                                        dataStore.setLeftImage(it.url.toString())
                                    }

                                }

                                CAR_RIGHT_IMAGE -> {
                                    lifecycleScope.launch {
                                        dataStore.setRightImage(it.url.toString())
                                    }
                                }

                                CAR_FRONT_IMAGE -> {
                                    lifecycleScope.launch {
                                        dataStore.setFrontImage(it.url.toString())
                                    }
                                }

                                CAR_BACK_IMAGE -> {
                                    lifecycleScope.launch {
                                        dataStore.setBackImage(it.url.toString())
                                    }
                                }

                                CAR_INSIDE_IMAGE -> {
                                    lifecycleScope.launch {
                                        dataStore.setInsideImage(it.url.toString())
                                    }
                                }

                                else -> {}
                            }
                        } else {
                            showFailAlert(it.message)
                        }
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${resource.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.no_internet_connection))
                }

                else -> {}
            }
        }
    }

    private fun setVehicleManufacturerData(it: VehicleManufacturerResponse) {
        modelList.addAll(it.data)
        manufactureModelList.clear()
        manufactureModelList.add(0, getString(R.string.select_vehicle_manufacture))
        for (list in 0 until modelList.size) {
            manufactureModelList.add(modelList[list].manufacturerName!!)
        }

        /***Vehicle Manufacturer spinner* */
        manufacturerNameAdapter =
            ArrayAdapter(activity, android.R.layout.simple_spinner_item, manufactureModelList)
        manufacturerNameAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVehicleManufactuer.adapter = manufacturerNameAdapter

        try {
            binding.spVehicleManufactuer.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        selectCarPos = position
                        if (selectCarPos != 0) {
                            selectVehicleManufactuer = true
                            selectCarPos = if (selectCarPos == 1) {
                                position
                            } else {
                                position - 1
                            }
                        } else {
                            selectVehicleManufactuer = false
                        }
                        vehicleModelList.clear()
                        binding.spVehicleManufactuer.setSelection(position)
                        manufactureName = modelList[selectCarPos].manufacturerName.toString()
                        vehicleManufacureId = modelList[selectCarPos].id.toString()
                        lifecycleScope.launch {
                            dataStore.setManufactureName(manufactureName)
                            dataStore.setManufactureId(vehicleManufacureId)
                        }

                        selectVehicleManufactuer = selectCarPos == 0
                        setVehicleModel(selectCarPos)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

            binding.spVehicleModel.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        var pos = position
                        /*if (pos != 0) {
                            pos = if (pos == 1) {
                                position
                            } else {
                                position - 1
                            }
                        }*/
                        if (pos > 0) {
                            pos -= 1
                            selectVehicleModel = true
                        } else {
                            selectVehicleModel = false
                        }
                        binding.spVehicleModel.setSelection(position)
                        // vehicleModelAdapter?.notifyDataSetChanged()
                        selectVehicleModel = pos == 0
                        try {
                            binding.tvVehicleType.text =
                                modelList[selectCarPos].vehicleModel?.get(pos)!!.vehicleTypeName
                            vehicleTypeModelNameModel =
                                modelList[selectCarPos].vehicleModel?.get(pos)!!.vehicleTypeModelName.toString()
                            vehicleModelId =
                                modelList[selectCarPos].vehicleModel?.get(pos)!!.id.toString()
                            vehicleType =
                                modelList[selectCarPos].vehicleModel?.get(pos)!!.vehicleTypeId.toString()
                        } catch (e: Exception) {
                            Log.e("TAG", "onItemSelected: ${e.message.toString()}")
                        }
                        lifecycleScope.launch {
                            dataStore.setVehicleModelName(vehicleTypeModelNameModel)
                            dataStore.setVehicleModelId(vehicleModelId)
                            dataStore.setVehicleType(vehicleType)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }


        /*** Year spinner* */
        manufactureYearList.add(0, getString(R.string.select_manufacture_year))
        manufactureYearList.addAll(it.yearList)

        manufactureYearAdapter =
            ArrayAdapter(activity, android.R.layout.simple_spinner_item, manufactureYearList)
        manufactureYearAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spVehicleYear.adapter = manufactureYearAdapter

        binding.spVehicleYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                if (binding.spVehicleYear.selectedItemId.toInt() > 0) {
                    selectVehicleYear = true
                }
                binding.spVehicleYear.setSelection(binding.spVehicleYear.selectedItemId.toInt())
                vehicleYear = manufactureYearList[position]
                selectVehicleYear = position == 0
                lifecycleScope.launch {
                    dataStore.setVehicleYear(vehicleYear)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setVehicleModel(position: Int) {
        vehicleModelList.clear()
        vehicleModelList.add(0, "select vehicle model")
        for (list in 0 until modelList[position].vehicleModel!!.size) {
            binding.tvVehicleType.text = modelList[position].vehicleModel?.get(0)?.vehicleTypeName
            vehicleModelList.add(modelList[position].vehicleModel?.get(list)?.vehicleTypeModelName!!)
        }
        vehicleModelAdapter?.notifyDataSetChanged()
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
        } else {
            /*below android 13 device */
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    fun selectImage(view: View) {
        when (view.id) {
            R.id.iv_carImage_left -> {
                IMAGE_CAPTURE_CODE = CAR_LEFT_IMAGE_CODE
                selectLeftImage = true
                requestPermissions()
            }

            R.id.iv_carImage_right -> {
                IMAGE_CAPTURE_CODE = CAR_RIGHT_IMAGE_CODE
                selectRightImage = true
                requestPermissions()
            }

            R.id.iv_carImage_front -> {
                IMAGE_CAPTURE_CODE = CAR_FRONT_IMAGE_CODE
                selectFrontImage = true
                requestPermissions()
            }

            R.id.iv_carImage_back -> {
                IMAGE_CAPTURE_CODE = CAR_BACK_IMAGE_CODE
                selectBackImage = true
                requestPermissions()
            }

            R.id.iv_carImage_inside -> {
                IMAGE_CAPTURE_CODE = CAR_INSIDE_IMAGE_CODE
                selectInsideImage = true
                requestPermissions()
            }
        }
    }


    private fun openCameraInterface() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getString(R.string.take_picture))
        values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.take_a_discription))
        imageUri =
            activity.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraResultLauncher.launch(intent)
    }

    private fun requestCameraPermission(): Boolean {
        var permissionGranted = false
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, open camera
            permissionGranted = true
            // Permission already granted, open camera
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
        return permissionGranted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && requestCode == RESULT_OK) {
            askCameraPermission()
        }
        if (requestCode == STORAGE_PERMISSION_CODE && requestCode == RESULT_OK) {
            askStoragePermission()
        }
    }

    private fun askCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun askStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                STORAGE_PERMISSION_CODE
            )
        }
    }
}


