package com.example.showfadriverletest.fragment

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.showfadriverletest.BR
import com.example.showfadriverletest.R
import com.example.showfadriverletest.base.BaseFragment
import com.example.showfadriverletest.component.showFailAlert
import com.example.showfadriverletest.databinding.FragmentPastBinding
import com.example.showfadriverletest.fragment.viewmodel.PastViewModel
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.pasttrip.TempResponse
import com.example.showfadriverletest.ui.mytrip.adapter.PastTripAdapter
import com.example.showfadriverletest.ui.tripdetail.TripDetailActivity
import com.example.showfadriverletest.util.CommonFun
import com.example.showfadriverletest.view.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PastFragment : BaseFragment<FragmentPastBinding, PastViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_past
    override val bindingVariable: Int
        get() = BR.viewModel
    private var list: ArrayList<TempResponse.DataItem> = ArrayList()
    private lateinit var layoutManager: LayoutManager
    var isScroll = false
    var isLastPage = false
    var currentItem = 0
    var totalItem = 0
    var scrollItem = 0
    var lastItemPos = 0
    var pageData: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIdFun()
        initRv(pageData)
        binding.pastTripRv.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int,
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    currentItem = layoutManager.childCount
                    totalItem = layoutManager.itemCount
                    scrollItem = layoutManager.findFirstVisibleItemPosition()
                    lastItemPos = layoutManager.findLastVisibleItemPosition()
                    if (isScroll && (currentItem + scrollItem == totalItem)) {
                        isScroll = false
                        if (!isLastPage){
                            pageData++
                            initRv(pageData)
                            binding.pastTripRv.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        })
    }

    override fun setupObservable() {
        viewModel.setNavigator(this)
        viewModel.getPastResponseObserver().observe(this) {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    Log.d(ContentValues.TAG, "on success=>${it.data!!.status}")
                    myDialog.hide()
                    isScroll = true
                    if (it.data.status) {
                        if (it.data.data.isEmpty()) {
                            if(pageData>1){
                                isLastPage = true
                            }else{
                                binding.tvNoDataFound.visibility = View.VISIBLE
                            }
                        } else {
                            binding.tvNoDataFound.visibility = View.GONE
                            list.addAll(it.data.data)
                            val pastAdapter = PastTripAdapter(requireContext(), list) { list, timeStamp ->
                                    lifecycleScope.launch {
                                        dataStore.setRatingImage(list.customerProfileImage.toString())
                                        dataStore.setCustomerFirstName(list.customerFirstName.toString())
                                        dataStore.setCustomerLastName(list.customerLastName.toString())
                                    }
                                    setIntentDataForTripActivity(list, timeStamp)
                                }
                            binding.pastTripRv.layoutManager = layoutManager
                            binding.pastTripRv.adapter = pastAdapter
                            binding.pastTripRv.layoutManager?.scrollToPosition(totalItem)
                        }
                    } else {
                        activity.showFailAlert(it.message!!)
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

    private fun setIntentDataForTripActivity(
        list: TempResponse.DataItem,
        timeStamp: String,
    ) {
        if (list != null) {
            val intent = Intent(requireContext(), TripDetailActivity::class.java)
            intent.putExtra("pickUpAddress", list.pickupLocation)
            intent.putExtra("dropOffAddress", list.dropoffLocation)
            intent.putExtra("pickupLat", list.pickupLat)
            intent.putExtra("pickupLag",list.pickupLng)
            intent.putExtra("dropLat", list.dropoffLat)
            intent.putExtra("dropLng", list.dropoffLng)
            intent.putExtra("tripTime", timeStamp)
            intent.putExtra("kilometer", list.distance)
            intent.putExtra("id", list.id)
            intent.putExtra("savingWallet",list.savingWalletAmount)
            intent.putExtra("serviceType", list.vehicleName)
            intent.putExtra("image",list.customerProfileImage)
            requireContext().startActivity(intent)
        }
    }

    private fun getIdFun() {
        dataStore.getUserId.asLiveData().observe(requireActivity()) {
            viewModel.id = it.toString()
        }
    }

    private fun initRv(pageData: Int) {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        viewModel.callPastTripApi(pageData)
    }
}
