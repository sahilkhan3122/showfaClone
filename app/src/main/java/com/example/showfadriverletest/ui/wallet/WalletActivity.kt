package com.example.showfadriverletest.ui.wallet

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.component.showSuccessAlert
import com.example.showfadriverletest.databinding.ActivityWalletBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.wallet.WalletHistoryResponse
import com.example.showfadriverletest.ui.cardselect.CardSelection
import com.example.showfadriverletest.ui.wallet.adapter.WalletAdapter
import com.example.showfadriverletest.ui.wallet.viewmodel.WalletViewModel
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.view.showSnackBar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WalletActivity : BaseActivity<ActivityWalletBinding, WalletViewModel>() {

    override val layoutId: Int
        get() = R.layout.activity_wallet
    override val bindingVariable: Int
        get() = BR.viewModel
    var pageData: Int = 1
    private var list: ArrayList<WalletHistoryResponse.DataItem> = ArrayList()
    private lateinit var layoutManager: LayoutManager
    private lateinit var walletAdapter: WalletAdapter
    private lateinit var uid: String
    var isScroll = false
    var isLastPage = false
    var currentItem = 0
    var totalItem = 0
    var scrollItem = 0
    var lastItemPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            getUserId()
        }
        binding.header.tvTitle.text = getString(R.string.wallet)
        binding.header.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }


        binding.rvTransferHistory.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    val layoutManager =
                        recyclerView.layoutManager as LinearLayoutManager
                    currentItem = layoutManager.childCount
                    totalItem = layoutManager.itemCount
                    scrollItem = layoutManager.findFirstVisibleItemPosition()
                    lastItemPos = layoutManager.findLastVisibleItemPosition()
                    if (isScroll && (currentItem + scrollItem == totalItem)) {
                        isScroll = false
                        if (!isLastPage) {
                            pageData++
                            initRv(pageData)
                            walletAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    private suspend fun getUserId() {
        val job = lifecycleScope.launch {
            dataStore.getUserId.asLiveData().observe(this@WalletActivity) {
                uid = it
                mViewModel.uid = uid
            }
            delay(100L)
        }
        job.join()
        initRv(pageData)
    }

    @SuppressLint("SetTextI18n")
    override fun setUpObserver() {
        mViewModel.setNavigator(this)
        mViewModel.getWalletHistoryObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    isScroll = true
                    if (it.data?.status == true) {
                        if (it.data.data!!.isEmpty()) {
                            if (pageData > 1) {
                                isLastPage = true
                            } else {
                                binding.rvTransferHistory.visibility = View.GONE
                                binding.tvNoDataFound.visibility = View.VISIBLE
                            }
                        } else {
                            binding.tvWalletBallence.text =
                                "KES :${it.data.walletBalance.toString()}"
                            binding.tvWalletUpdate.text =
                                "Last Updated:${it.data.lastWalletUpdateTime.toString()}"
                            it.data.data?.let { it1 -> list.addAll(it1) }
                            walletAdapter = WalletAdapter(this@WalletActivity, list) { _ -> }
                            binding.rvTransferHistory.layoutManager = layoutManager
                            binding.rvTransferHistory.adapter = walletAdapter
                            binding.rvTransferHistory.layoutManager?.scrollToPosition(totalItem)
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
                }

                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.please_check_internet_connection))
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                }

                else -> {}
            }
        }

        mViewModel.getSendMoneyObservable().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                  /*  showSuccessAlert(it.data?.walletBalance.toString())*/
                        Toast.makeText(this, "${it.data?.message}", Toast.LENGTH_SHORT).show()
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
                }
                Resource.Status.LOADING -> {
                    myDialog.show()
                    Log.d(ContentValues.TAG, "loading=>${it.message}")
                }

                Resource.Status.NO_INTERNET_CONNECTION -> {
                    myDialog.hide()
                    binding.root.showSnackBar(getString(R.string.please_check_internet_connection))
                    Log.d(ContentValues.TAG, "no internet=>${it.message}")
                }

                else -> {}
            }
        }
    }

    private fun initRv(pageData: Int) {
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mViewModel.callWalletHistoryApi(pageData, uid)
    }

    fun walletMenu(view: View) {
        when (view.id) {
            R.id.ivAddMoney -> {
                openCloseAddMoney()
            }

            R.id.ivSendMoney -> {
                openCloseSendMoney()
            }

            R.id.ivWithdraw -> {
                with(binding) { root.showSnackBar(getString(R.string.withdraw)) }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun openCloseSendMoney() {
        val bsSendMoneyDialog = BottomSheetDialog(this@WalletActivity, R.style.TransparentDialog)
        bsSendMoneyDialog.setContentView(R.layout.dialog_send_money)
        val ivCloseAddMoneyDialog =
            bsSendMoneyDialog.findViewById<ImageView>(R.id.ivCloseSendMoneyDialog)
        val etEnterAmount = bsSendMoneyDialog.findViewById<TextView>(R.id.etEnterAmount)
        val etPhoneNumber = bsSendMoneyDialog.findViewById<TextView>(R.id.etPhoneNumber)
        val sendMoney = bsSendMoneyDialog.findViewById<MaterialTextView>(R.id.tv_bt_send_money)
        ivCloseAddMoneyDialog!!.setOnClickListener { bsSendMoneyDialog.dismiss() }

        sendMoney!!.setOnClickListener {

            val amount = etEnterAmount!!.text.toString()
            val number = etPhoneNumber!!.text.toString()
            val type = "driver"
            if (TextUtils.isEmpty(amount)) {
                Toast.makeText(this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT)
                    .show()
            } else if (TextUtils.isEmpty(number)) {
                Toast.makeText(
                    this,
                    getString(R.string.msg_enter_mobile_number),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                mViewModel.callSendMoneyApi(amount, number, type, uid)
                bsSendMoneyDialog.dismiss()
            }
        }
        bsSendMoneyDialog.show()
    }

    private fun openCloseAddMoney() {
        val bsAddMoneyDialog = BottomSheetDialog(this, R.style.TransparentDialog)
        bsAddMoneyDialog.setContentView(R.layout.dialog_add_money)
        val addMoney = bsAddMoneyDialog.findViewById<TextView>(R.id.tv_bt_add_money)
        val ivCloseAddMoneyDialog =
            bsAddMoneyDialog.findViewById<ImageView>(R.id.ivCloseAddMoneyDialog)
        val etEnterAddAmount = bsAddMoneyDialog.findViewById<EditText>(R.id.etEnterAddAmount)

        ivCloseAddMoneyDialog!!.setOnClickListener { bsAddMoneyDialog.dismiss() }

        addMoney!!.setOnClickListener {
            val amount = etEnterAddAmount!!.text.toString()
            if (TextUtils.isEmpty(amount)) {
                Toast.makeText(this, getString(R.string.please_enter_amount), Toast.LENGTH_SHORT)
                    .show()
            } else {
                startActivity(
                    Intent(this, CardSelection::class.java).putExtra(
                        "amount", amount
                    ).putExtra("title", getString(R.string.proceed_to_pay)).putExtra("id", uid)
                )
                bsAddMoneyDialog.dismiss()
            }
        }
        bsAddMoneyDialog.show()
    }
}