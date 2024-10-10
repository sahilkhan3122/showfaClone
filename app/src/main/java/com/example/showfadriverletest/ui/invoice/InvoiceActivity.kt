package com.example.showfadriverletest.ui.invoice

import android.os.Bundle
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.databinding.ActivityInvoiceBinding
import com.example.showfadriverletest.ui.invoice.viewmodel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceActivity : BaseActivity<ActivityInvoiceBinding, InvoiceViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_invoice
    override val bindingVariable: Int
        get() = BR.viewModel

    private var pickUpAddress: String? = null
    private var dropOffAddress: String? = null
    private var pickLat = ""
    private var pickLng = ""
    private var dropLat = ""
    private var dropLng = ""
    private var tripTime = ""
    private var kilometer = ""
    private var id = ""
    private var saving = ""
    private var totalPay = ""
    private var serviceType = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backFun()
        getIntentData()
    }

    private fun getIntentData() {
        if (intent != null) {
            pickUpAddress = intent.getStringExtra("pickUpAddress")
            dropOffAddress = intent.getStringExtra("dropOffAddress")
            pickLat = intent.getStringExtra("pickupLat").toString()
            pickLng = intent.getStringExtra("pickupLag").toString()
            dropLat = intent.getStringExtra("dropLat").toString()
            dropLng = intent.getStringExtra("dropLng").toString()
            tripTime = intent.getStringExtra("tripTime").toString()
            kilometer = intent.getStringExtra("kilometer").toString()
            id = intent.getStringExtra("id").toString()
            totalPay = intent.getStringExtra("totalPay").toString()
            saving = intent.getStringExtra("savingWallet").toString()
            serviceType = intent.getStringExtra("serviceType").toString()
            setInvoiceData()
        }
    }

    private fun backFun() {
        binding.apply {
            header.tvTitle.text = getString(R.string.invoice_activity)
            header.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
            }
        }
    }

    override fun setUpObserver() {}

    private fun setInvoiceData() {
        binding.apply {
            pickupDropOff.tvPickupLocation.text = pickUpAddress
            pickupDropOff.tvDropOffLocation.text = dropOffAddress
            trpiDetail.tvTripDuration.text = tripTime
            trpiDetail.tvGrandTotal.text = kilometer.plus(getString(R.string.kms))
            tvTripTime.text = tripTime
            tvSavingWalletAmount.text = getString(R.string.kesCurreny).plus(":").plus(saving)
            tvTotalPayable.text = getString(R.string.kesCurreny).plus(":").plus(totalPay)
            tvServiceType.text = serviceType
        }
    }
}