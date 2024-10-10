package com.example.showfadriverletest.response.sendmoney

import com.example.showfadriverletest.response.BaseResponse
import com.google.gson.annotations.SerializedName

data class SendMoneyResponse(

    @field:SerializedName("wallet_balance")
    val walletBalance: Int? = null,
) : BaseResponse()
