package com.example.showfadriverletest.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.common.ApiConstant.FlagMapDirectionState
import com.example.showfadriverletest.common.ApiConstant.PIN
import com.example.showfadriverletest.common.ApiConstant.STATUS_REQUEST_ACCEPT
import com.example.showfadriverletest.common.ApiConstant.STATUS_REQUEST_TRAVELING
import com.example.showfadriverletest.common.ApiConstant.bookingRequestSlide
import com.example.showfadriverletest.common.Constants
import com.example.showfadriverletest.common.Constants.PASSANGER_NUMBER
import com.example.showfadriverletest.common.Constants.SOS_NUMBER
import com.example.showfadriverletest.common.Constants.isSwitchOn
import com.example.showfadriverletest.common.Constants.latitude
import com.example.showfadriverletest.common.Constants.longitude
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.component.showSuccessAlert
import com.example.showfadriverletest.databinding.ActivityMapsBinding
import com.example.showfadriverletest.di.app.MyApplication
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.BaseResponse
import com.example.showfadriverletest.response.OnAcceptBookingResponse
import com.example.showfadriverletest.response.OnForwardBookingRequest
import com.example.showfadriverletest.response.OnInitResponse
import com.example.showfadriverletest.response.canceltrip.CancelModel
import com.example.showfadriverletest.response.drawermodel.DrawerModel
import com.example.showfadriverletest.response.login.LoginResponse
import com.example.showfadriverletest.service.LocationService
import com.example.showfadriverletest.service.TAG
import com.example.showfadriverletest.ui.chat.ChatActivity
import com.example.showfadriverletest.ui.completetripRating.CompleteTripActivity
import com.example.showfadriverletest.ui.earning.EarningActivity
import com.example.showfadriverletest.ui.edit.editProfile.EditProfileActivity
import com.example.showfadriverletest.ui.home.adapter.CancelTripAdapter
import com.example.showfadriverletest.ui.home.adapter.DrawerAdapter
import com.example.showfadriverletest.ui.mytrip.MyTripActivity
import com.example.showfadriverletest.ui.notification.NotificationActivity
import com.example.showfadriverletest.ui.savingwallet.SavingWalletActivity
import com.example.showfadriverletest.ui.setting.SettingActivity
import com.example.showfadriverletest.ui.subscription.SubscriptionActivity
import com.example.showfadriverletest.ui.support.SupportActivity
import com.example.showfadriverletest.ui.termscondition.TermsOfService
import com.example.showfadriverletest.ui.wallet.WalletActivity
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.DateUtils
import com.example.showfadriverletest.util.DateUtils.USA_FORMAT_DATE
import com.example.showfadriverletest.util.GpsUtil
import com.example.showfadriverletest.util.PopupDialog
import com.example.showfadriverletest.util.PopupDialog.initPopup
import com.example.showfadriverletest.util.PopupDialog.socketDailog
import com.example.showfadriverletest.util.SnackbarUtil
import com.example.showfadriverletest.util.gone
import com.example.showfadriverletest.util.mapparam.MapData
import com.example.showfadriverletest.util.setGlideImage
import com.example.showfadriverletest.util.showMsgDialog
import com.example.showfadriverletest.util.startNewActivity
import com.example.showfadriverletest.util.visible
import com.example.showfadriverletest.view.showSnackBar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MapsActivity : BaseActivity<ActivityMapsBinding, HomeViewModel>(), OnMapReadyCallback {
    override val layoutId: Int
        get() = R.layout.activity_maps
    override val bindingVariable: Int
        get() = BR.viewModel


    private var cancellationAdapter: CancelTripAdapter? = null
    private var acceptGsonData: OnAcceptBookingResponse? = null
    private var initGsonData: OnInitResponse? = null
    private var acceptRideTimer: CountDownTimer? = null
    private var bsCustomerRequestBehavior: BottomSheetBehavior<*>? = null
    private var cancelReasonBehaviour: BottomSheetBehavior<*>? = null
    private lateinit var vibrator: Vibrator
    private val vibrationDuration = 30000L
    var notifySound: MediaPlayer? = null
    private var latLng: LatLng? = null
    private var carMarker: Marker? = null
    var handlerAnimateMarker: Handler? = null
    private var mSocket: Socket? = null
    private var repeatJobUpdateDriverLocation: Job? = null
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var serviceRegistered = false
    private var isOpen = false
    private var locationPermission = false
    private val option = MarkerOptions()
    private var reason = ""
    var driverId = ""
    var firstRequestId = ""
    var bookingType = ""
    var bookingId = ""
    var delay = 1000
    private var driverName = ""
    private lateinit var locationService: LocationService
    private lateinit var launcher: ActivityResultLauncher<String>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var gpsUtils: GpsUtil
    private lateinit var drawableModel: ArrayList<DrawerModel>
    private lateinit var locationCallback: LocationCallback
    private var cancellationModel = ArrayList<CancelModel>()
    private val PERMISSION_REQUEST_CODE = 1
    private val REQUEST_CALL_PHONE = 3
    private val overlayPermissionRequestCode = 123
    private val PERMISSION_REQUEST_CODE_BACKGROUND_PERMISSION = 2
    var distance = "4"
    var customerId = ""
    var completeTripDropOffLat = ""
    var completeTripDropOffLng = ""
    private var dropOffLat = ""
    private var dropOffLng = ""
    private var gpsStatus = ""
    var ModeChange = false
    var loginData: LoginResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setDrawerHeaderImage()
        activityLauncher()
        connectSocket()
        setUpOnClickListener()
        setBottomSheetBehaviour()
        checkStatus()
        setDrawerList()
    }

    private fun init() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationService = LocationService()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        gpsUtils = GpsUtil(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        onBackPressedDispatcher.addCallback(this@MapsActivity, callback)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    getLastLocation()
                }
            }
        }
        dataStore.getUserId.asLiveData().observe(this@MapsActivity) { driverId = it }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ApiConstant.GPS_REQUEST) {
            if (resultCode == RESULT_OK) {
                gpsStatus = "enable"
                getLastLocation()
            } else {
                gpsStatus = "disable"
            }
        }
    }

    private fun setBottomSheetBehaviour() {
        val customerRequestDialog = binding.bsCustomerRequestDialog.bsCustomerRequestDialog
        bsCustomerRequestBehavior = BottomSheetBehavior.from(customerRequestDialog)
        bsCustomerRequestBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        val cancelReasonDialog = binding.cancelResonDialog.bsCancelReason
        cancelReasonBehaviour = BottomSheetBehavior.from(cancelReasonDialog)
        cancelReasonBehaviour!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    @SuppressLint("MissingInflatedId")
    override fun setUpObserver() {
        /** logout api */
        mViewModel.getLogoutObserver().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                        Log.e("DeviceToken----------------", deviceToken.toString())
                        finishAffinity()
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    lifecycleScope.launch {
                        if (!checkIsSessionnOut(it.code, getString(R.string.session_expire))) {
                            it.message?.let { _ ->
                                if (CommonFun.checkIsConnectionReset(it.code)) {
                                    getString(R.string.no_internet)
                                } else {
                                }
                            }
                        }
                    }
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                    isInternetConnected = false
                }

                else -> {}
            }
        }
        /**changeDuty api*/
        mViewModel.getChangeDutyObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        if (it.data.duty == "online") {/* animateDriverStatus()*/
                            showSuccessAlert(getString(R.string.you_re_online))
                            connectSocket()
                            bindService()
                            // startService(Intent(applicationContext, FloatingViewService::class.java))
                            updateDriverLocationSocketCall()
                            binding.driverStatus.tvDriverStatus.text =
                                getString(R.string.you_re_online)
                            isSwitchOn = true
                        } else {/*    animateDriverStatus()*/
                            isSwitchOn = false
                            showFailAlert(getString(R.string.you_re_offline))
                            binding.driverStatus.tvDriverStatus.text =
                                getString(R.string.you_re_offline)
                            if (repeatJobUpdateDriverLocation != null) {
                                repeatJobUpdateDriverLocation!!.cancel()
                                repeatJobUpdateDriverLocation = null
                            }
                            if (isServiceRunningInForeground(LocationService::class.java)) {
                                unbindService(sc)
                            }
                            disconnectSocket()
                        }
                    } else {
                        showFailAlert(it.message!!)
                        binding.driverStatus.switchOnline.isChecked = false
                        binding.driverStatus.tvDriverStatus.text =
                            getString(R.string.you_re_offline)
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    if (it.code != 403) {
                        binding.driverStatus.switchOnline.isChecked = false
                        binding.driverStatus.tvDriverStatus.setText(R.string.you_re_offline)
                        showFailAlert(it.message.toString())

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
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                    binding.driverStatus.switchOnline.isChecked = false
                    binding.driverStatus.tvDriverStatus.setText(R.string.you_re_offline)
                    showFailAlert(getString(R.string.please_check_internet_connection))
                    isInternetConnected = false
                }

                else -> {}
            }
        }
        /**cancel Trip api*/
        mViewModel.getCancelTripObserver().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
                        binding.cancelResonDialog.cancelReson.gone()
                        binding.driverStatus.clStartRideRootStatus.visible()
                        socketDailog(this, it.message!!)

                        if (mMap != null) {
                            mMap!!.clear()/* getCurrentLocation()*/
                            getLastLocation()
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
                                if (CommonFun.checkIsConnectionReset(it.code)) {
                                    getString(R.string.no_internet)
                                } else {
                                    message
                                }
                            }
                        }
                    }
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
                    showFailAlert(it.message!!)
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                    binding.root.showSnackBar(getString(R.string.no_internet_connection))
                }

                else -> {}
            }
        }
        /**completeTrip api*/
        mViewModel.getCompleteTripObserver().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        Log.e("CompleteTripResponse: ", it.data.data.toString())
                        acceptRideTimer?.cancel()
                        binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
                        binding.driverStatus.clStartRideRootStatus.visible()
                        if (mMap != null) {
                            mMap!!.clear()
                            getLastLocation()
                        }
                        val (minutes, remainingSeconds) = convertSecondsStringToMinutesAndSeconds(it?.data.data?.duration!!)
                        val intent = Intent(this, CompleteTripActivity::class.java)
                        intent.putExtra("passangerImage", it.data.data.passengerPhoto)
                        intent.putExtra("bookingId", it.data.data.id)
                        intent.putExtra("passangerName", it?.data.data.passengerFullName)
                        intent.putExtra("passangerRating", it?.data.data.passengerRating)
                        intent.putExtra(
                            "tripTime",
                            minutes.toString() + "m" + remainingSeconds.toString() + "s"
                        )
                        intent.putExtra("waitingTime", it?.data.data.waitingTime)
                        intent.putExtra("distance", it?.data.data.distance)
                        intent.putExtra("waitingFee", it?.data.data.waitingTimeCharge)
                        intent.putExtra("earning", it?.data.data.grandTotal)
                        intent.putExtra(
                            "savingWalletAmount", it?.data.data.customerSavingWalletAmount
                        )
                        startActivity(intent)
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
                    lifecycleScope.launch {
                        if (!checkIsSessionnOut(it.code, getString(R.string.session_expire))) {
                            it.message?.let { message ->
                                if (CommonFun.checkIsConnectionReset(it.code)) {
                                    getString(R.string.no_internet)
                                } else {
                                    message
                                }
                            }
                        }
                    }
                    showFailAlert(it.message!!)
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                    binding.root.showSnackBar(getString(R.string.no_internet_connection))
                }

                else -> {}
            }
        }
    }

    private fun checkStatus() {
        val bookingData = gson.fromJson(ApiConstant.initBookingInfo, LoginResponse::class.java)
        Log.d("checkStatus", "checkStatus: ${bookingData}")
        if (bookingData != null) {
            if (bookingData.bookingInfo!!.driverDuty == "1") {
                isSwitchOn = true
                binding.driverStatus.switchOnline.isChecked = true
                bindService()
                updateDriverLocationSocketCall()
                binding.driverStatus.tvDriverStatus.text = getString(R.string.you_re_online)
            } else {
                isSwitchOn = false
                binding.driverStatus.switchOnline.isChecked = false
                binding.driverStatus.tvDriverStatus.text = getString(R.string.you_re_offline)
            }
            if (bookingData.bookingInfo.status != null) {
                when (bookingData.bookingInfo.status) {
                    /**  if booking status is in accepted state*/
                    STATUS_REQUEST_ACCEPT -> {
                        FlagMapDirectionState = "AcceptState"
                        if (bookingData.bookingInfo.arrivedTime == null || bookingData.bookingInfo.arrivedTime == "") {
                            binding.bsCustomerRequestDialog.bsCustomerRequestDialog.visible()
                            binding.driverStatus.clStartRideRootStatus.gone()
                            bookingRequestSlide = 2
                            Log.e("killApp Accept gson Data: ", bookingData.toString())
                            try {
                                Handler(Looper.myLooper()!!).postDelayed({
                                    if (latitude != 0.0 && longitude != 0.0) {
                                        setMapPolyLine(
                                            latitude.toString(),
                                            longitude.toString(),
                                            bookingData.bookingInfo.pickupLat.toString(),
                                            bookingData.bookingInfo.pickupLng.toString()
                                        )
                                    }
                                }, 3000)

                            } catch (e: Exception) {
                                Log.e(TAG, "onAcceptBookingRequest = ${e.message}")
                            }
                            val dataBookingInfo = bookingData.bookingInfo
                            val dataCustomerInfo = dataBookingInfo.customerInfo
                            bookingId = bookingData.bookingInfo.id.toString()
                            driverName = bookingData.bookingInfo.customerInfo?.firstName.toString()
                            driverId = bookingData.bookingInfo.driverInfo?.id.toString()
                            customerId = bookingData.bookingInfo.customerId.toString()
                            val img = ApiConstant.BASE_URL + dataCustomerInfo?.profileImage
                            setPreferenceArriveData(dataBookingInfo, dataCustomerInfo, img)
                            stopMusic()
                            stopVibration()
                        } else if (bookingData.bookingInfo.arrivedTime.isNotEmpty()) {
                            FlagMapDirectionState = "StartTrip"
                            bookingId = bookingData.bookingInfo.id.toString()
                            dropOffLat = bookingData.bookingInfo.dropoffLat.toString()
                            dropOffLng = bookingData.bookingInfo.dropoffLng.toString()
                            binding.bsCustomerRequestDialog.bsCustomerRequestDialog.visible()
                            binding.bsCustomerRequestDialog.ivRedirectionToGoogleMap.gone()
                            PIN = bookingData.bookingInfo.pin.toString()
                            bookingRequestSlide = 3
                            Handler(Looper.myLooper()!!).postDelayed({
                                setMapPolyLine(
                                    bookingData.bookingInfo.pickupLat.toString(),
                                    bookingData.bookingInfo.pickupLng.toString(),
                                    bookingData.bookingInfo.driverInfo?.lat.toString(),
                                    bookingData.bookingInfo.driverInfo?.lng.toString()
                                )
                            }, 3000)
                            val dataBookingInfo = bookingData.bookingInfo
                            val dataCustomerInfo = dataBookingInfo.customerInfo
                            val img = ApiConstant.BASE_URL + dataCustomerInfo?.profileImage
                            setPreferenceArriveSlideData(
                                dataBookingInfo, dataCustomerInfo, img
                            )
                        }
                    }

                    STATUS_REQUEST_TRAVELING -> {
                        FlagMapDirectionState = "StartTrip"
                        binding.driverStatus.clStartRideRootStatus.gone()
                        bookingId = bookingData.bookingInfo.id.toString()
                        binding.bsCustomerRequestDialog.bsCustomerRequestDialog.visible()
                        PIN = bookingData.bookingInfo.pin.toString()
                        bookingRequestSlide = 4
                        val dataBookingInfo = bookingData.bookingInfo
                        val dataCustomerInfo = dataBookingInfo.customerInfo
                        val img = ApiConstant.BASE_URL + dataCustomerInfo?.profileImage
                        setPreferenceCompleteSlideData(
                            dataBookingInfo, dataCustomerInfo, img
                        )
                        completeTripDropOffLat = bookingData.bookingInfo.dropoffLat!!
                        completeTripDropOffLng = bookingData.bookingInfo.dropoffLng!!
                        dropOffLat = bookingData.bookingInfo.dropoffLat.toString()
                        dropOffLng = bookingData.bookingInfo.dropoffLng.toString()
                        try {
                            Handler(Looper.myLooper()!!).postDelayed({
                                setMapPolyLine(
                                    bookingData.bookingInfo.pickupLat,
                                    bookingData.bookingInfo.pickupLng,
                                    bookingData.bookingInfo.dropoffLat,
                                    bookingData.bookingInfo.dropoffLng
                                )
                            }, 3000)
                        } catch (e: Exception) {
                            Log.e(TAG, "onAcceptBookingRequest = ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun setPreferenceCompleteSlideData(
        dataBookingInfo: LoginResponse.BookingInfo,
        dataCustomerInfo: LoginResponse.CustomerInfo?,
        img: String,
    ) {
        binding.bsCustomerRequestDialog.apply {
            tvTripAmount.text = getString(R.string.kesDoller).plus(dataBookingInfo.estimatedFare)
            tvCustomerName.text = dataCustomerInfo?.firstName
            tvCustomerRating.text = dataCustomerInfo!!.rating
            tvPaymentType.text = getString(R.string.pyment_type).plus(dataBookingInfo.paymentType)
            pickupData.tvPickupLocation.text = dataBookingInfo.pickupLocation
            pickupData.tvDropOffLocation.text =
                dataBookingInfo.dropoffLocation/*  userPreferences?.isShowForwardDialog = "true"*/
            setGlideImage(img, ivCustomerProfile, progressBar)
            tvTime.text = DateUtils.timeStampToUsaDate(
                dataBookingInfo.bookingTime.toString(), USA_FORMAT_DATE
            )
            tvWaitingTime.gone()
            llRideAmountDetails.gone()
            llCustomerContact.gone()
            ivCancelRide.gone()
            slideText.text = getString(R.string.complete_trip)
            setSliderTextAlpha(0)
        }
    }

    private fun setPreferenceArriveSlideData(
        dataBookingInfo: LoginResponse.BookingInfo,
        dataCustomerInfo: LoginResponse.CustomerInfo?,
        img: String,
    ) {
        binding.bsCustomerRequestDialog.apply {
            tvTripAmount.text = getString(R.string.kesDoller).plus(dataBookingInfo.estimatedFare)
            tvCustomerName.text = dataCustomerInfo?.firstName
            tvCustomerRating.text = dataCustomerInfo!!.rating
            tvPaymentType.text = getString(R.string.pyment_type).plus(dataBookingInfo.paymentType)
            pickupData.tvPickupLocation.text = dataBookingInfo.pickupLocation
            pickupData.tvDropOffLocation.text = dataBookingInfo.dropoffLocation
            setGlideImage(img, ivCustomerProfile, progressBar)
            tvTime.text = DateUtils.timeStampToUsaDate(
                dataBookingInfo.bookingTime.toString(), USA_FORMAT_DATE
            )
            tvWaitingTime.gone()
            llRideAmountDetails.gone()
            llCustomerContact.visible()
            ivCancelRide.gone()
            slideText.text = getString(R.string.start_trip)
            setSliderTextAlpha(0)
        }
    }

    private fun setPreferenceArriveData(
        dataBookingInfo: LoginResponse.BookingInfo?,
        dataCustomerInfo: LoginResponse.CustomerInfo?,
        img: String,
    ) {
        binding.bsCustomerRequestDialog.apply {
            tvTripAmount.text = getString(R.string.kesDoller).plus(dataBookingInfo!!.estimatedFare)
            tvCustomerName.text = dataCustomerInfo?.firstName
            tvCustomerRating.text = dataCustomerInfo!!.rating
            tvPaymentType.text = getString(R.string.pyment_type).plus(dataBookingInfo.paymentType)
            pickupData.tvPickupLocation.text = dataBookingInfo.pickupLocation
            pickupData.tvDropOffLocation.text = dataBookingInfo.dropoffLocation
            setGlideImage(img, ivCustomerProfile, progressBar)
            tvTime.text = DateUtils.timeStampToUsaDate(
                dataBookingInfo.bookingTime.toString(), USA_FORMAT_DATE
            )
            tvWaitingTime.gone()
            llRideAmountDetails.gone()
            llCustomerContact.visible()
            ivCancelRide.gone()
            slideText.text = getString(R.string.arrived)
            setSliderTextAlpha(0)
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                    "package:$packageName"
                )
            )
            startActivityForResult(
                intent, overlayPermissionRequestCode
            )
        }
    }

    private fun requestPermissionBackgroundPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ), PERMISSION_REQUEST_CODE_BACKGROUND_PERMISSION
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ), PERMISSION_REQUEST_CODE_BACKGROUND_PERMISSION
                )
            }
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("call", "onRequestPermissionsResult() 11 = $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPermissionBackgroundPermission()
                    Log.e("call", "onRequestPermissionsResult() 22 = ")
                } else {
                    PopupDialog.logout(
                        this, resources.getString(R.string.permission_denied_for_location)
                    ) {
                        Handler(Looper.myLooper()!!).postDelayed({
                            PopupDialog.logout(
                                this,
                                resources.getString(R.string.permission_denied_for_location_settings)
                            ) {
                                Handler(Looper.myLooper()!!).postDelayed({
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                                    intent.data = Uri.parse("package:$packageName")
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                    startActivityForResult(intent, 1000)
                                    finish()
                                }, 400)
                            }
                        }, 400)
                    }
                }
            }

            1000, PERMISSION_REQUEST_CODE_BACKGROUND_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("call", "onRequestPermissionsResult() 22 = ")
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    ) {
                        PopupDialog.logout(
                            this, resources.getString(R.string.permission_denied_for_location)
                        ) {}

                    } else {
                        PopupDialog.logout(
                            this, resources.getString(R.string.permission_denied_for_location)
                        ) {
                            Handler(Looper.myLooper()!!).postDelayed({
                                PopupDialog.logout(
                                    this,
                                    resources.getString(R.string.permission_denied_for_location_settings)
                                ) {
                                    Handler(Looper.myLooper()!!).postDelayed({
                                        val intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                                        intent.data = Uri.parse("package:$packageName")
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        startActivityForResult(intent, 1000)
                                        finish()
                                    }, 400)
                                }
                            }, 400)
                        }
                    }
                }
            }

            REQUEST_CALL_PHONE -> {}
        }
    }

    /** <--------------------------------------------------------------   driver duty on or off  ---------------------------------------------------------------> */
    private fun onSwitchChange(latLng: LatLng) {
        mViewModel.changeDutyApiCall(latLng, driverId)
    }

    /** <--------------------------------------------------------------   Socket on  ---------------------------------------------------------------> */

    private val onConnect: Emitter.Listener = Emitter.Listener {
        runOnUiThread {
            Log.e(TAG, "onConnect:")
        }
    }
    private val onUpdateDriverLatLong: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            Log.e(TAG, "onUpdateDriverLatLong = ${args[0]}")
        }
    }
    private val onReceiveBookingRequest: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            Log.e(TAG, "onForwardBookingRequest = ${args[0]}")
            val intent = Intent(
                this@MapsActivity, MapsActivity::class.java
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            bookingRequestSlide = 1

            if (args[0].toString()
                    .isNotEmpty() && firstRequestId.isEmpty() && firstRequestId != gson.fromJson(
                    args[0].toString(), OnForwardBookingRequest::class.java
                ).bookingInfo?.id.toString()
            ) {
                binding.driverStatus.clStartRideRootStatus.gone()
                try {

                    if (isPhoneSilent()) {
                        startVibration()
                    } else {
                        notifySound = MediaPlayer.create(this, R.raw.pick_and_go)
                        if (notifySound != null) {
                            notifySound?.start()
                            notifySound!!.isLooping = true
                        }
                    }

                } catch (e: Exception) {
                    if (e.localizedMessage != null) {
                        e.localizedMessage?.let { showFailAlert(it) }
                        Log.e(TAG, "Exception notifySound: ${e.localizedMessage}")
                    }
                }
                val gsonData =
                    gson.fromJson(args[0].toString(), OnForwardBookingRequest::class.java)
                initGsonData = gson.fromJson(args[0].toString(), OnInitResponse::class.java)
                Log.e(TAG, "forwardBookingResponse: $gsonData")
                Log.e(TAG, "inResponse: $initGsonData")
                PASSANGER_NUMBER = gsonData.bookingInfo!!.customerInfo!!.mobileNo.toString()
                firstRequestId = gsonData.bookingInfo.id.toString()
                driverName = gsonData?.bookingInfo.customerInfo?.firstName.toString()
                bookingId = gsonData.bookingInfo.id.toString()
                bookingType = gsonData.bookingInfo.bookingType.toString()
                lifecycleScope.launch {
                    mViewModel.bookingId = bookingId
                    dataStore.setBookingId(bookingId)
                    dataStore.setBookingType(bookingType)
                }
                Log.e(TAG, "onForwardBookingRequest in: $firstRequestId")
                if (gsonData != null) {
                    binding.bsCustomerRequestDialog.bsCustomerRequestDialog.visible()
                    acceptRideTimerStart()
                    setBottomSheetCustomerRequestDialog(gsonData)
                }
            } else {
                if (firstRequestId != gson.fromJson(
                        args[0].toString(), OnForwardBookingRequest::class.java
                    ).bookingInfo?.id.toString()
                ) {

                    firstRequestId = gson.fromJson(
                        args[0].toString(), OnForwardBookingRequest::class.java
                    ).bookingInfo?.id.toString()
                    lifecycleScope.launch {
                        dataStore.setBookingId(firstRequestId)
                    }
                    val i = JSONObject()
                    i.put(ApiConstant.driverId, driverId)
                    i.put(ApiConstant.bookingId, firstRequestId)
                    Log.e(
                        TAG,
                        "onForwardBookingRequest emit forwardBookingRequestToAnotherDriver :  $i"
                    )
                    stopMusic()
                    stopVibration()
                    binding.driverStatus.clStartRideRootStatus.visible()
                    mSocket?.emit(ApiConstant.forwardBookingRequestToAnotherDriver, i)
                    Log.e(TAG, "onForwardBookingRequest out: $firstRequestId")
                }
            }
        }
    }
    private val onAcceptBookingRequest: Emitter.Listener = Emitter.Listener { args ->
        Log.e(TAG, "onAcceptBookingRequest = ${args[0]}")
        runOnUiThread {
            Constants.IS_NOTIFICATION_ACCEPTED_TRIP = true
            firstRequestId = ""
            if (args[0].toString().isNotEmpty()) {
                stopMusic()
                stopVibration()
                acceptGsonData =
                    gson.fromJson(args[0].toString(), OnAcceptBookingResponse::class.java)
                bookingId = acceptGsonData?.bookingInfo?.id.toString()
                driverId = acceptGsonData?.bookingInfo?.driverInfo?.id.toString()
                customerId = acceptGsonData?.bookingInfo?.customerId.toString()
                if (acceptGsonData != null) {
                    try {
                        setMapPolyLine(
                            latLng?.latitude.toString(),
                            latLng?.longitude.toString(),
                            acceptGsonData?.bookingInfo?.pickupLat,
                            acceptGsonData?.bookingInfo?.pickupLng
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "onAcceptBookingRequest = ${e.message}")
                    }
                }
            }
        }
    }
    private val onCancelBookingBeforeAccept: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            if (mSocket!!.connected()) {
                try {
                    val json = JSONObject(args[0].toString())
                    Log.e(TAG, "onCancelBookingBeforeAccept() json:- $json")
                    acceptRideTimer?.cancel()
                    stopMusic()
                    stopVibration()
                    firstRequestId = ""
                    binding.driverStatus.clStartRideRootStatus.visible()
                    binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
                    showFailAlert(json.getString("message").trim { it <= ' ' })
                    showFailAlert(json.getString("message").trim { it <= ' ' })
                } catch (e: Exception) {
                    Log.e(TAG, "Exception : ${e.message}")
                }
            }
        }
    }
    private val onArrivedPickUpLocation: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            Log.e(TAG, "onArrivedPickUpLocation = ${args[0]}")
            acceptGsonData = gson.fromJson(args[0].toString(), OnAcceptBookingResponse::class.java)
            PIN = acceptGsonData!!.bookingInfo.pin.toString()
            Log.e(TAG, "PIN----------------: $PIN")
            binding.bsCustomerRequestDialog.ivRedirectionToGoogleMap.gone()

        }
    }
    private val onStartTrip: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            Log.e("onStartTrip", "onStartTrip = ${args[0]}")/*complete Trip Api param*/
            completeTripDropOffLat = acceptGsonData?.bookingInfo?.dropoffLat.toString()
            dropOffLat = acceptGsonData?.bookingInfo?.dropoffLat.toString()
            completeTripDropOffLng = acceptGsonData?.bookingInfo?.dropoffLng.toString()
            dropOffLng = acceptGsonData?.bookingInfo?.dropoffLng.toString()
            distance = acceptGsonData?.bookingInfo?.distanceFare.toString()
            acceptGsonData = gson.fromJson(args[0].toString(), OnAcceptBookingResponse::class.java)
            if (mMap != null) {
                mMap!!.clear()
                binding.bsCustomerRequestDialog.ivRedirectionToGoogleMap.visible()
                setMapPolyLine(
                    acceptGsonData!!.bookingInfo.pickupLat,
                    acceptGsonData!!.bookingInfo.pickupLng,
                    acceptGsonData!!.bookingInfo.dropoffLat,
                    acceptGsonData!!.bookingInfo.dropoffLng
                )
            }
        }
    }
    private val cancelTrip: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            firstRequestId = ""
            Log.e(TAG, "cancelTrip = ${args[0]}")
            if (mMap != null) {
                mMap!!.clear()
            }
            acceptRideTimer?.cancel()
            val gsonData = gson.fromJson(args[0].toString(), BaseResponse::class.java)
            /*  showFailAlert(gsonData.message)*/
            bookingRequestSlide = 1
            binding.bsCustomerRequestDialog.slideText.text = getString(R.string.slide_to_accept)
            binding.driverStatus.clStartRideRootStatus.visible()
            binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
            binding.bsCustomerRequestDialog.apply {
                tvWaitingTime.visible()
                llRideAmountDetails.visible()
                llCustomerContact.gone()
                ivCancelRide.visible()
            }
            binding.bsCustomerVerifyOtp.bsVerifyCustomer.gone()
            binding.cancelResonDialog.cancelReson.gone()
        }
    }
    private val onDisconnect: Emitter.Listener = Emitter.Listener { args ->
        runOnUiThread {
            Log.e(TAG, "onDisconnect = ${args[0]}")

        }
    }

    private fun isPhoneSilent(): Boolean {
        /*  val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager*/
        val am = getSystemService(AUDIO_SERVICE) as AudioManager

        when (am.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                Log.i("MyApp", "Silent mode")
                am.ringerMode == AudioManager.RINGER_MODE_SILENT
                ModeChange = true
            }

            AudioManager.RINGER_MODE_VIBRATE -> {
                am.ringerMode == AudioManager.RINGER_MODE_VIBRATE
                ModeChange = true
            }

            AudioManager.MODE_RINGTONE -> {
                am.ringerMode == AudioManager.RINGER_MODE_NORMAL
                ModeChange = false
            }
        }
        return ModeChange
    }

    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 1000), // Vibrate for 1000 milliseconds (1 second)
                0 // No repeat
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationDuration)
        }
    }

    private fun stopVibration() {
        if (vibrator != null) {
            vibrator.cancel()
        }
    }

    /** <--------------------------------------------------------------   *socket function  ---------------------------------------------------------------> */
    private fun startTripFun() {
        openVerifyCustomerOtp()
    }

    private fun openVerifyCustomerOtp() {
        binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
        binding.bsCustomerVerifyOtp.bsVerifyCustomer.visible()
        binding.bsCustomerVerifyOtp.apply {
            btSubmitOTP.setOnClickListener {
                if (etEnterCustomerOTP.text.toString() == PIN) {
                    binding.bsCustomerVerifyOtp.bsVerifyCustomer.gone()
                    binding.bsCustomerRequestDialog.bsCustomerRequestDialog.visible()
                    binding.bsCustomerRequestDialog.llCustomerContact.gone()
                    binding.bsCustomerRequestDialog.slideText.text =
                        getString(R.string.complete_trip)
                    etEnterCustomerOTP.text.clear()
                    setSliderTextAlpha(0)
                    emitStartTripSocketCall()
                } else {
                    showFailAlert(getString(R.string.invalid_pin))
                }
            }
        }
    }

    private fun setUpOnClickListener() {
        binding.apply {
            Drawer.setOnClickListener { setDrawerOpenClose() }
            drawableItems.header.setOnClickListener { startNewActivity(EditProfileActivity::class.java) }
            driverStatus.resetCurrentLocation.setOnClickListener { getLastLocation() }
            drawableItems.logout.setOnClickListener { logout() }
            driverStatus.switchOnline.setOnCheckedChangeListener { _, _ -> setDriverDutyStatus() }
            bsCustomerRequestDialog.ivRedirectionToGoogleMap.setOnClickListener { redirectionToGoogleMap() }
            bsCustomerRequestDialog.tvCancel.setOnClickListener {
                setCancelBottomSeatBehaviour()
                setCancelList()
                setRecycleViewForCancelBottomSeat()
            }
            bsCustomerRequestDialog.slideSeek.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    /** Here, you can update UI or perform actions as the SeekBar progresses*/
                    setSliderTextAlpha(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    /**This is called when the user starts touching the SeekBar*/
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    /**This is called when the user stops touching the SeekBar*/
                    if (seekBar?.progress == 100) {
                        when (bookingRequestSlide) {
                            1 -> {
                                bookingRequestSlide = 2
                                acceptRideFunction()
                                seekBar.progress = 0
                                stopMusic()
                            }

                            2 -> {
                                seekBar.progress = 0
                                bookingRequestSlide = 3
                                arriveDriverFunction()
                            }

                            3 -> {
                                startTripFun()
                                driverStatus.clStartRideRootStatus.gone()
                                bookingRequestSlide = 4
                                seekBar.progress = 0
                            }

                            4 -> {
                                initPopup(
                                    this@MapsActivity,
                                    getString(R.string.are_you_sure_you_want_to_complete_trip)
                                ) {
                                    mViewModel.completeTripApiCall(
                                        bookingId,
                                        completeTripDropOffLat,
                                        completeTripDropOffLng,
                                        distance
                                    )
                                }
                                bsCustomerRequestDialog.slideText.text =
                                    getString(R.string.slide_to_accept)
                                bookingRequestSlide = 1
                                bsCustomerRequestDialog.tvWaitingTime.visible()
                                bsCustomerRequestDialog.ivCancelRide.visible()
                                seekBar.progress = 0
                            }
                        }
                    }
                }
            })
        }
    }

    private fun setRecycleViewForCancelBottomSeat() {
        binding.apply {
            val recyclerView = cancelResonDialog.cancellationItemRecyclerView

            layoutManager =
                LinearLayoutManager(this@MapsActivity, LinearLayoutManager.VERTICAL, false)
            cancellationAdapter =
                CancelTripAdapter(this@MapsActivity, cancellationModel) { _, cancelmodel ->
                    Log.d("list", "checkbox==>${cancelmodel.isCheck}")
                    reason = cancelmodel.textCancellationName
                    val builder = StringBuilder()
                    builder.append(reason)
                    reason = builder.toString()
                }
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = cancellationAdapter
            cancelResonDialog.btnGoBack.setOnClickListener {
                cancelResonDialog.cancelReson.visibility = View.GONE
            }
            cancelResonDialog.btnCancelRide.setOnClickListener {
                mViewModel.cancelTripAPiCall(bookingId, reason)
            }
        }
    }

    private fun setCancelList() {
        cancellationModel = arrayListOf()
        cancellationModel.add(
            CancelModel(
                false, getString(R.string.driver_delay)
            )
        )
        cancellationModel.add(
            CancelModel(
                false, getString(R.string.changedtheplan)
            )
        )
        cancellationModel.add(
            CancelModel(
                false, getString(R.string.longpickuptime)
            )
        )
        cancellationModel.add(
            CancelModel(
                false, getString(R.string.nolonginterest)
            )
        )

        cancellationModel.add(
            CancelModel(
                false, getString(R.string.other)
            )
        )
    }

    private fun setCancelBottomSeatBehaviour() {
        val viewDialogBottomSheet =
            activity.layoutInflater.inflate(R.layout.cancel_reson_item, null)
        val bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bsCancelReason))
        bottomSheetBehavior.peekHeight = resources.displayMetrics.heightPixels / 2
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        viewDialogBottomSheet.visibility = View.VISIBLE
        binding.cancelResonDialog.cancelReson.visibility = View.VISIBLE
    }

    private fun redirectionToGoogleMap() {
        if (FlagMapDirectionState == "AcceptState") {
            setMapDirection(latitude, longitude)
        }
        if (FlagMapDirectionState == "StartTrip") {
            setMapDirection(
                dropOffLat.toDouble(), dropOffLng.toDouble()
            )
        }
    }

    private fun setDriverDutyStatus() {
        binding.apply {
            if (!isLocationEnabled()) {
                Toast.makeText(this@MapsActivity, "please enable location", Toast.LENGTH_SHORT)
                    .show()
                driverStatus.switchOnline.isChecked = false
                isSwitchOn = false
                driverStatus.tvDriverStatus.setText(R.string.you_re_offline)
            } else if (!hasOverlayPermission()) {
                driverStatus.switchOnline.isChecked = false
                driverStatus.tvDriverStatus.setText(R.string.you_re_offline)
                requestOverlayPermission()
            } else {
                if (driverId != "" && driverId.isNotEmpty()) {
                    latLng?.let { onSwitchChange(it) }
                }
            }
        }
    }

    private fun setDrawerOpenClose() {
        if (!isOpen) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            isOpen = true
        } else {
            isOpen = false
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun repeatJobUpdateDriverLocation(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
                try {
                    emitDriverLocationUpdate()
                } catch (e: Exception) {
                    Log.e("repeatJobUpdateDriverLocation Exception", "$e")
                }
                delay(timeInterval)
            }
        }
    }

    private fun setBottomSheetCustomerRequestDialog(gsonData: OnForwardBookingRequest) {
        binding.bsCustomerRequestDialog.apply {
            val dataBookingInfo = gsonData.bookingInfo
            val dataCustomerInfo = dataBookingInfo?.customerInfo
            val img = ApiConstant.Image_URL + dataCustomerInfo?.profileImage
            tvTripAmount.text = getString(R.string.kesDoller).plus(dataBookingInfo!!.estimatedFare)
            tvCustomerName.text = dataCustomerInfo?.firstName
            tvCustomerRating.text = dataCustomerInfo!!.rating
            tvPaymentType.text = getString(R.string.pyment_type).plus(dataBookingInfo.paymentType)
            pickupData.tvPickupLocation.text = dataBookingInfo.pickupLocation
            pickupData.tvDropOffLocation.text = dataBookingInfo.dropoffLocation
            setGlideImage(img, ivCustomerProfile, progressBar)
            tvTime.text = DateUtils.timeStampToUsaDate(
                dataBookingInfo.bookingTime.toString(), USA_FORMAT_DATE
            )
        }
    }

    private fun setMapDirection(latitude: Double, longitude: Double) {
        val directionsBuilder =
            Uri.Builder().scheme("https").authority("www.google.com").appendPath("maps")
                .appendPath("dir").appendPath("").appendQueryParameter("api", "1")
                .appendQueryParameter(
                    "destination", "${latitude},${longitude}"
                )
        startActivity(Intent(Intent.ACTION_VIEW, directionsBuilder.build()))
    }

    private fun arriveDriverFunction() {
        binding.bsCustomerRequestDialog.slideText.text = getString(R.string.start_trip)
        emitArrivedAtPickupLocation()
    }

    private fun acceptRideFunction() {
        binding.bsCustomerRequestDialog.apply {
            acceptRideTimer?.cancel()
            tvWaitingTime.gone()
            llRideAmountDetails.gone()
            llCustomerContact.visible()
            ivCancelRide.gone()
            slideText.text = getString(R.string.arrived)
            emitAcceptBookingSocketCall()
            setSliderTextAlpha(0)
            binding.driverStatus.clStartRideRootStatus.gone()
        }
    }

    private fun stopMusic() {
        try {
            if (notifySound != null) {
                notifySound?.stop()
                notifySound?.release()
                notifySound?.isLooping = false
                notifySound = null
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "stopMusic: ${e.message}")
        }
    }

    private fun setSliderTextAlpha(progress: Int) {
        val remaining = 100 - progress
        val getAlpha = remaining.toFloat() / 100
        binding.bsCustomerRequestDialog.slideText.alpha = getAlpha
    }

    private fun connectSocket() {
        try {
            val app: MyApplication = application as MyApplication
            mSocket = app.getMSocket()
            if (mSocket != null) {
                mSocket?.connect()
                mSocket?.on(Socket.EVENT_CONNECT, onConnect)
                mSocket?.on(ApiConstant.SOCKET_ON_UPDATE_DRIVER_LAT_LONG, onUpdateDriverLatLong)
                mSocket?.on(ApiConstant.SOCKET_ON_BOOKING_REQUEST_BOOK_NOW, onReceiveBookingRequest)
                mSocket?.on(
                    ApiConstant.SOCKET_ON_CANCEL_BOOKING_BEFORE_ACCEPT,
                    onCancelBookingBeforeAccept
                )
                mSocket?.on(ApiConstant.SOCKET_ON_CANCEL_TRIP, cancelTrip)
                mSocket?.on(ApiConstant.SOCKET_ON_ACCEPT_BOOKING_BOOK_NOW, onAcceptBookingRequest)
                mSocket?.on(
                    ApiConstant.SOCKET_ON_ARRIVED_AT_PICKUP_LOCATION,
                    onArrivedPickUpLocation
                )
                mSocket?.on(ApiConstant.SOCKET_ON_BOOKING_INFO_BOOK_NOW_START_TRIP, onStartTrip)
                mSocket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (mSocket?.connected() == true) {
                    Log.e(TAG, "Socket Connected")
                    connectUserEmit()
                } else {
                    IO.Options().reconnection = true
                    Log.e(TAG, "Socket not connected, reconnecting....")
                }
            }, delay * 2.toLong())
        } catch (e: Exception) {
            Log.e(TAG, "connectSocket:Exception ${e.message}")
        }
    }


    private fun connectUserEmit() {
        if (mSocket?.connected() == true) {
            val j = JSONObject()
            j.put(ApiConstant.DRIVER_ID, driverId)
            Log.e(TAG, "emit connect user parameter $j")
            mSocket?.emit(ApiConstant.ConnectUser, j)
        }
    }

    private fun disconnectSocket() {
        try {
            if (mSocket?.connected() == true) {
                mSocket?.off(Socket.EVENT_CONNECT, onConnect)
                mSocket?.off(
                    ApiConstant.SOCKET_ON_UPDATE_DRIVER_LAT_LONG, onUpdateDriverLatLong
                )
                mSocket?.off(
                    ApiConstant.SOCKET_ON_BOOKING_REQUEST_BOOK_NOW, onReceiveBookingRequest
                )
                mSocket?.off(
                    ApiConstant.SOCKET_ON_CANCEL_BOOKING_BEFORE_ACCEPT, onCancelBookingBeforeAccept
                )
                mSocket?.off(
                    ApiConstant.SOCKET_ON_ACCEPT_BOOKING_BOOK_NOW, onAcceptBookingRequest
                )
                mSocket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
            }

        } catch (e: Exception) {
            Log.e(TAG, "disconnectSocket: Exception ${e.message}")
        }
    }

    /*-vihicle data show---
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
            binding.txtTimer.visibility = View.GON
        }
    }*/

    private fun acceptRideTimerStart() {
        if (acceptRideTimer != null) {
            acceptRideTimer = null
        }
        acceptRideTimer = object : CountDownTimer(35000, 1000) {
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
                binding.bsCustomerRequestDialog.tvWaitingTime.text = hms
            }

            override fun onFinish() {
                firstRequestId = ""
                binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
                binding.driverStatus.clStartRideRootStatus.visible()
                try {
                    if (notifySound != null) {
                        notifySound!!.stop()
                        notifySound!!.release()
                        notifySound = null
                    }
                    stopVibration()
                } catch (e: Exception) {
                    if (e.localizedMessage != null) {
                        e.localizedMessage?.let { showFailAlert(it) }
                        Log.e(TAG, "Exception notifySound: ${e.localizedMessage}")
                    }
                }
            }
        }
        (acceptRideTimer as CountDownTimer).start()
    }

    override fun onDestroy() {
        stopMusicAndSocket()
        super.onDestroy()
    }

    private fun stopMusicAndSocket() {
        /* stopService(Intent(applicationContext, FloatingViewService::class.java))*/
        bookingRequestSlide = 0
        stopMusic()
        stopVibration()
        try {
            if (repeatJobUpdateDriverLocation != null) {
                repeatJobUpdateDriverLocation?.cancel()
                repeatJobUpdateDriverLocation = null
            }
            disconnectSocket()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: ${e.message}")
        }
    }

    /** <--------------------------------------------------------------   socket emit function  ---------------------------------------------------------------> */
    private fun emitDriverLocationUpdate() {

        if (mSocket != null && mSocket!!.connected()) {
            val obj = JSONObject()
            try {
                obj.put(ApiConstant.SOCKET_PARAM_DRIVER_ID, driverId)
                obj.put(ApiConstant.SOCKET_PARAM_DRIVER_LAT, latitude)
                obj.put(ApiConstant.SOCKET_PARAM_DRIVER_LONG, longitude)
                obj.put(ApiConstant.DEVICE_TOKEN, deviceToken)
                mSocket?.emit(ApiConstant.SOCKET_EMIT_UPDATE_DRIVER_LAT_LONG, obj)
                Log.e(TAG, "emitDriverLocationUpdate: $obj")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateDriverLocationSocketCall() {
        repeatJobUpdateDriverLocation = if (repeatJobUpdateDriverLocation == null) {
            repeatJobUpdateDriverLocation(delay * 5.toLong())
        } else {
            repeatJobUpdateDriverLocation?.cancel()
            null
        }
    }

    fun forwardRequestToAnotherDriver(view: View) {
        emitForwardRequestToAnotherDriver()
        firstRequestId = ""
        acceptRideTimer?.cancel()
        binding.driverStatus.clStartRideRootStatus.visible()
        binding.bsCustomerRequestDialog.bsCustomerRequestDialog.gone()
        Log.d(TAG, "onCancelClick: ")
        stopMusic()
        stopVibration()
    }

    private fun emitForwardRequestToAnotherDriver() {
        val i = JSONObject()
        if (driverId.isNotEmpty() && bookingId.isNotEmpty()) {
            i.put(ApiConstant.driverId, driverId)
            i.put(ApiConstant.bookingId, bookingId)
            Log.e(TAG, "emit forwardBookingRequestToAnotherDriver :  $i")
            mSocket?.emit(ApiConstant.forwardBookingRequestToAnotherDriver, i)
            binding.driverStatus.clStartRideRootStatus.visible()
        }
    }

    private fun emitAcceptBookingSocketCall() {
        stopMusic()
        FlagMapDirectionState = "AcceptState"
        Handler(Looper.myLooper()!!).postDelayed({
            val i = JSONObject()
            if (userId != null) {
                i.put(ApiConstant.driverId, driverId)
                i.put(ApiConstant.bookingId, bookingId)
                i.put(ApiConstant.bookingType, bookingType)
                Log.e(TAG, "emit acceptBookingRequest :  $i")
                mSocket?.emit(ApiConstant.acceptBookingRequest, i)
            }
        }, 0)
    }

    private fun emitArrivedAtPickupLocation() {
        val i = JSONObject()
        if (bookingId != null) {
            i.put(ApiConstant.bookingId, bookingId)
            Log.e(TAG, "emit arrivedAtPickupLocation :  $i")
            mSocket?.emit(ApiConstant.arrivedAtPickupLocation, i)
        }
    }

    private fun emitStartTripSocketCall() {
        FlagMapDirectionState = "StartTrip"
        Handler(Looper.myLooper()!!).postDelayed({
            val i = JSONObject()
            if (bookingId.isNotEmpty()) {
                i.put(ApiConstant.bookingId, bookingId)
                Log.e(TAG, "emit startTrip :  $i")
                mSocket?.emit(ApiConstant.startTrip, i)
            }
        }, 2000)
    }

    private fun bindService() {
        serviceRegistered = true
        val i = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i)
        } else {
            startService(i)
        }
        bindService(i, sc, BIND_AUTO_CREATE)
    }

    private val sc: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    fun logout() {
        PopupDialog.logout(this, getString(R.string.are_yor_sure_you_want_to_logoutt)) {
            mViewModel.callLogoutApi(driverId)
        }
    }

    private fun activityLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    locationPermission = true
                    getLastLocation()
                }
            }
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getLastLocation() {
        if (locationPermission) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    Log.d("LOCATION", "location==>$location")
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latLng = LatLng(location.latitude, location.longitude)
                        if (mMap != null) {
                            carMarker?.remove()
                            latLng?.let { carMarker(it) }
                        }
                        Log.d(ContentValues.TAG, "LAT---->${location.latitude}")
                        Log.d(ContentValues.TAG, "LNG---->${location.longitude}")
                    }
                }
            } else {

                activity.showMsgDialog(activity.resources.getString(R.string.location_permissions_needed),
                    activity.resources.getString(R.string.open_setting),
                    { _, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    },
                    "Cancel",
                    { _, _ ->

                    })
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest, locationCallback, Looper.myLooper()
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun carMarker(latLng: LatLng) {
        if (mMap != null) {
            carMarker = mMap!!.addMarker(
                option.position(latLng)
                    .icon(resizeBitmap(R.drawable.car_red.toString(), 60, 110)?.let { it1 ->
                        BitmapDescriptorFactory.fromBitmap(
                            it1
                        )
                    })
            )
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 22f)) ?: ""
            val cameraPosition =
                CameraPosition.Builder().target(latLng) // Specify the center point of the map
                    .zoom(18f).build()
            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500, null)
            if (handlerAnimateMarker == null) {
                handlerAnimateMarker = Handler(Looper.getMainLooper())
                handlerAnimateMarker?.postDelayed(object : Runnable {
                    override fun run() {
                        handlerAnimateMarker?.postDelayed(this, delay * 3.toLong())
                    }
                }, 0)
            }
        }
    }

    private fun iconMarker(latLng: LatLng) {
        if (mMap != null) {
            mMap!!.addMarker(
                option.position(latLng).icon(resizeBitmap(
                    R.drawable.live_marker_img.toString(), 210, 210
                )?.let { it1 ->
                    BitmapDescriptorFactory.fromBitmap(
                        it1
                    )
                })
            )
        }
    }

    private fun setDrawerHeaderImage() {
        dataStore.getDataStore.asLiveData().observe(this) {
            loginData = gson.fromJson(it, LoginResponse::class.java)
            loginData?.data?.apply {
                Glide.with(this@MapsActivity).load(ApiConstant.Image_URL.plus(profileImage))
                    .into(binding.drawableItems.headerImage)
                binding.drawableItems.txtFirstName.text = loginData!!.data?.firstName
                binding.drawableItems.txtLastName.text = loginData!!.data?.lastName
            }
            Log.e("setPrefillData--------------------------", "setPrefillData: $loginData")
        }
    }

    private fun setDrawerList() {
        drawableModel = arrayListOf()
        drawableModel.add(DrawerModel(R.drawable.ic_menu_history, "Trip History"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_earnings, "Earning"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_subscription, "Membership"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_wallet, "Wallet"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_wallet, "Saving Wallet"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_settings, "Settings"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_support, "Support"))
        drawableModel.add(DrawerModel(R.drawable.ic_menu_privacy_policy, "legal"))

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val drawerAdapter = DrawerAdapter(this, drawableModel) { position ->
            when (position) {
                0 -> {

                    startNewActivity(MyTripActivity::class.java)
                }

                1 -> {
                    startNewActivity(EarningActivity::class.java)
                }

                2 -> {
                    startNewActivity(SubscriptionActivity::class.java)
                }

                3 -> {
                    startNewActivity(WalletActivity::class.java)
                }

                4 -> {
                    startNewActivity(SavingWalletActivity::class.java)
                }

                5 -> {
                    startNewActivity(SettingActivity::class.java)
                }

                6 -> {
                    startNewActivity(SupportActivity::class.java)
                }

                7 -> {
                    startNewActivity(TermsOfService::class.java)
                }
            }
        }
        binding.drawableItems.drawerRecyclerView.layoutManager = layoutManager
        binding.drawableItems.drawerRecyclerView.adapter = drawerAdapter
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
    }

    fun notificationList(view: View) {
        startNewActivity(NotificationActivity::class.java)
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            stopMusicAndSocket()
            finishAffinity()
        }
    }

    private fun isServiceRunningInForeground(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return service.foreground
            }
        }
        return false
    }

    fun sosClick(view: View) {
        if (SOS_NUMBER.isNotEmpty() && SOS_NUMBER != "") {
            callSos(SOS_NUMBER)
        } else {
            SnackbarUtil.show(this, R.string.sos_not_found.toString(), Snackbar.LENGTH_LONG)
        }
    }

    private fun callSos(sosMobNumber: String) {
        Log.e(TAG, "callSos() SosMobNumber:- $sosMobNumber")
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sosMobNumber"))
        startActivity(dialIntent)
    }

    private fun setMapPolyLine(
        pickupLat: String?,
        pickupLng: String?,
        dropOffLat: String?,
        dropOffLng: String?,
    ) {

        val startLocation = LatLng(pickupLat!!.toDouble(), pickupLng!!.toDouble())
        val destinationLocation = LatLng(dropOffLat!!.toDouble(), dropOffLng!!.toDouble())
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 18f)) ?: ""
        latLng?.let { carMarker(it) }
        iconMarker(destinationLocation)
        val url = getDirectionURL(
            startLocation,
            destinationLocation,
            "AIzaSyAHoxA9mAOwiilUUwLfauECc7SrJSNwywM"
        )
        GetDirection(url).execute()
        Log.d(ContentValues.TAG, "startLocation====>${startLocation}")
        Log.d(ContentValues.TAG, "destinationLocation====>${destinationLocation}")
    }


    private fun getDirectionURL(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false" + "&mode=driving" + "&key=$secret"
    }

    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path = ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }

                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineOption = PolylineOptions()
            for (i in result.indices) {
                lineOption.addAll(result[i])
                lineOption.width(10f)
                lineOption.color(getColor(R.color.black))
            }
            mMap!!.addPolyline(lineOption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }


    private fun resizeBitmap(drawableName: String?, width: Int, height: Int): Bitmap? {
        val imageBitmap = BitmapFactory.decodeResource(
            resources, resources.getIdentifier(
                drawableName, "drawable", packageName
            )
        )
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    fun callToCustomer(view: View) {
        callSos(PASSANGER_NUMBER)
    }

    fun chatToCustomer(view: View) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("bookingId", bookingId)
        intent.putExtra("driverId", driverId)
        intent.putExtra("customerId", customerId)
        intent.putExtra("driverName", driverName)
        startActivity(intent)
    }

    private fun convertSecondsStringToMinutesAndSeconds(secondsString: String): Pair<Long, Long> {
        val seconds = secondsString.toLong()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return Pair(minutes, remainingSeconds)
    }
}