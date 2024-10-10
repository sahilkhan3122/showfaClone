package com.example.showfadriverletest.ui.savingwallet

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseActivity
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.ActivitySavingWalletBinding
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.savingwallet.SavingWalletHistoryResponse
import com.example.showfadriverletest.ui.savingwallet.adapter.SavingWalletAdapter
import com.example.showfadriverletest.ui.savingwallet.viewmodel.SavingWalletViewModel
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavingWalletActivity : BaseActivity<ActivitySavingWalletBinding, SavingWalletViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_saving_wallet
    override val bindingVariable: Int
        get() = BR.viewModel

    var pageData: Int = 1
    private var list: ArrayList<SavingWalletHistoryResponse.DataItem> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var walletAdapter: SavingWalletAdapter
    private lateinit var uid: String
    var isScroll = false
    var currentItem = 0
    var totalItem = 0
    var scrollItem = 0
    var lastItemPos = 0
    var lastPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getId()
        backFun()
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
                        Handler(Looper.myLooper()!!).postDelayed({
                            if(!lastPage){
                                pageData += 1
                                initRv(pageData)
                                walletAdapter.notifyDataSetChanged()
                            }
                        }, 1000)
                    }
                }
            }
        })
    }

    private fun getId() {
        lifecycleScope.launch {
            getUserId()
        }
    }

    private fun backFun() {
        binding.header.tvTitle.text = getString(R.string.saving_wallet)
        binding.header.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
    }


    private suspend fun getUserId() {
        val job = lifecycleScope.launch {
            dataStore.getUserId.asLiveData().observe(this@SavingWalletActivity) {
                uid = it.toString()
            }
            delay(100L)
        }
        job.join()
        initRv(pageData)
    }


    override fun setUpObserver() {
        mViewModel.setNavigator(this)
        mViewModel.getsavingWalletHistoryObservable().observe(this) {

            when (it.status) {
                Resource.Status.SUCCESS -> {
                    myDialog.hide()
                    isScroll = true
                    if (it.data?.status == true) {
                        if (it.data.data!!.isEmpty()) {
                            if(pageData>1){
                               lastPage = true
                            }else{
                                binding.rvTransferHistory.visibility = View.GONE
                                binding.tvNoDataFound.visibility = View.VISIBLE
                            }
                        } else {
                            binding.tvWalletBallence.text = getString(R.string.kesCurreny).plus(it.data.savingWalletBalance)
                            list.addAll(it.data.data)
                            walletAdapter = SavingWalletAdapter(this, list) { _ -> }
                            binding.rvTransferHistory.layoutManager = layoutManager
                            binding.rvTransferHistory.adapter = walletAdapter
                            binding.rvTransferHistory.layoutManager?.scrollToPosition(totalItem)
                        }
                    } else {
                        showFailAlert(it.message!!)
                    }
                }

                Resource.Status.ERROR -> {
                    Log.d(ContentValues.TAG, "on error----------------=>${it.message}")
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
        mViewModel.callSavingWalletHistoryApi(pageData, uid)
    }
}