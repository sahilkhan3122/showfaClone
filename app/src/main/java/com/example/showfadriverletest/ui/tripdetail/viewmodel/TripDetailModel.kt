package com.example.showfadriverletest.ui.tripdetail.viewmodel

import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.showfadriverletest.base.BaseViewModel
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.network.ApiService
import com.example.showfadriverletest.network.Resource
import com.example.showfadriverletest.response.init.InitResponse
import com.example.showfadriverletest.response.rating.ReviewListResponse
import com.example.showfadriverletest.util.NetworkUtils
import com.example.showfadriverletest.util.PrintLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.ConnectException
import javax.inject.Inject

class TripDetailModel  @Inject constructor(
    var context: Context,
    var networkService: ApiService
) : BaseViewModel<Any>() {

    private val reviewListResponseObserver: MutableLiveData<Resource<ReviewListResponse>> = MutableLiveData()
    fun getReviewListObservable(): LiveData<Resource<ReviewListResponse>> {
        return reviewListResponseObserver
    }

    fun reviewRatingApi(bookingId:String) {
        if (NetworkUtils.isNetworkConnected(context)) {
            lateinit var response: Response<ReviewListResponse>
            viewModelScope.launch {
                try {
                    reviewListResponseObserver.value = Resource.loading()
                    withContext(Dispatchers.IO) {
                        response =
                            networkService.reviewListApi(ApiConstant.REVIEW_LIST+"/"+bookingId)
                        PrintLog.e("showfa", "$response")
                    }

                    withContext(Dispatchers.Main) {
                        response.run {
                            reviewListResponseObserver.value = baseDataSource.getResult(true) { this }
                            Log.e(ContentValues.TAG, "${baseDataSource.getResult(true) { this }}")
                        }
                    }
                } catch (e: ConnectException) {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            reviewListResponseObserver.value = Resource(
                                Resource.Status.UNKNOWN,
                                null,
                                e.localizedMessage?.toString().plus("- " + e.cause),
                                502
                            )
                        }
                    }
                }catch (e: Exception) {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            reviewListResponseObserver.value = Resource(
                                Resource.Status.UNKNOWN,
                                null,
                                e.localizedMessage.let { "- " + e.cause },
                                500
                            )
                        }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    reviewListResponseObserver.value = Resource.noInternetConnection(null)
                }
            }
        }

    }
}

