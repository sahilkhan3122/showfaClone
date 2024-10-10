package com.example.showfadriverletest.response.rating

import com.example.showfadriverletest.response.BaseResponse
import com.google.gson.annotations.SerializedName

data class ReviewListResponse(

	@field:SerializedName("data")
	val data: Data? = null,


):BaseResponse()
{
	data class Data(

		@field:SerializedName("review_id")
		val reviewId: String? = null,

		@field:SerializedName("profile_image")
		val profileImage: String? = null,

		@field:SerializedName("rating")
		val rating: String? = null,

		@field:SerializedName("last_name")
		val lastName: String? = null,

		@field:SerializedName("created_at")
		val createdAt: String? = null,

		@field:SerializedName("comment")
		val comment: String? = null,

		@field:SerializedName("first_name")
		val firstName: String? = null
	)
}


