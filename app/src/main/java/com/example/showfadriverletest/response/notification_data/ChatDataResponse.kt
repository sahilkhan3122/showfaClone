package com.example.showfadriverletest.response.notification_data

import com.google.gson.annotations.SerializedName

data class ChatDataResponse(

	@field:SerializedName("booking_id")
	val bookingId: String? = null,

	@field:SerializedName("sender_type")
	val senderType: String? = null,

	@field:SerializedName("receiver_id")
	val receiverId: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("sender_id")
	val senderId: String? = null,

	@field:SerializedName("receiver_type")
	val receiverType: String? = null
)
