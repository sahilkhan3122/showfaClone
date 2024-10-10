package com.example.showfadriverletest.response

import com.google.gson.annotations.SerializedName

open class BaseResponse(

    @SerializedName("status")
    var status: Boolean = false,

    @SerializedName("message")
    var message: String = "",

    @SerializedName("error")
    var error: String = "",
) {
}