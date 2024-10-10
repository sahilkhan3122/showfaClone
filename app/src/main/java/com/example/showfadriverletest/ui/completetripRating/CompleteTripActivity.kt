package com.example.showfadriverletest.ui.completetripRating

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.ActivityCompleteTripBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.util.SnackbarUtil
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CompleteTripActivity : BaseActivity<ActivityCompleteTripBinding, CompleteTripViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_complete_trip
    override val bindingVariable: Int
        get() = BR.viewModel

    private var passangerImage = ""
    private var passangerName = ""
    private var passangerRating = ""
    private var tripTime = ""
    private var waitingTime = ""
    private var distance = ""
    private var waitingFee = ""
    private var earning = ""
    private var bookingId = ""
    private var savingWalletAmount = ""
    private var ratingToCustomer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.header.tvTitle.text = getString(R.string.ratingscreen)
        backFun()
        getIntentData()
        clickFun()
    }

    private fun clickFun() {
        binding.ratingBar.setOnRatingChangeListener { _, rating, _ ->
            ratingToCustomer = rating.toString()
        }

        binding.tvSubmit.setOnClickListener {
            var comment = binding.edtComment.text.toString()
            if (comment.isEmpty()) {
                comment = "null"
            }
            mViewModel.ratingResponseApi(bookingId, comment, ratingToCustomer)
        }
    }

    private fun getIntentData() {
        if (intent != null) {
            passangerImage = intent.getStringExtra("passangerImage").toString()
            passangerName = intent.getStringExtra("passangerName").toString()
            bookingId = intent.getStringExtra("bookingId").toString()
            passangerRating = intent.getStringExtra("passangerRating").toString()
            tripTime = intent.getStringExtra("tripTime").toString()
            waitingTime = intent.getStringExtra("waitingTime").toString()
            distance = intent.getStringExtra("distance").toString()
            waitingFee = intent.getStringExtra("waitingFee").toString()
            earning = intent.getStringExtra("earning").toString()
            savingWalletAmount = intent.getStringExtra("savingWalletAmount").toString()
        }
        setData(
            passangerImage,
            passangerName,
            passangerRating,
            tripTime,
            waitingTime,
            waitingFee,
            earning,
            distance
        )
    }

    private fun backFun() {
        binding.header.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
    }

    private fun setData(
        passangerImage: String,
        passangerName: String,
        passangerRating: String,
        tripTime: String,
        waitingTime: String,
        waitingFee: String,
        earning: String,
        distance: String,
    ) {
        binding.apply {
            val profileImage = ApiConstant.Image_URL.plus(passangerImage)
            customerName.text = passangerName
            tvRating.text = passangerRating
            binding.tripTime.text = tripTime
            binding.waitingTime.text = waitingTime
            binding.distance.text = distance.plus(getString(R.string.kms))
            waitingFees.text = waitingFee
            grandTotal.text = getString(R.string.kesCurreny).plus(earning)
            Glide.with(this@CompleteTripActivity)
                .load(profileImage)
                .into(imageProfile)
        }
    }

    override fun setUpObserver() {
        mViewModel.getReviewRatingObserver().observe(this) {
            Resource
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    if (it.data?.status == true) {
                        finish()
                    } else {
                        it.message?.let { it1 -> SnackbarUtil.show(this, it1) }
                    }
                }

                Resource.Status.ERROR -> {
                    myDialog.hide()
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
                    lifecycleScope.launch {
                        if (!checkIsSessionnOut(
                                it.code,
                                getString(R.string.session_expire)
                            )
                        ) {
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
}