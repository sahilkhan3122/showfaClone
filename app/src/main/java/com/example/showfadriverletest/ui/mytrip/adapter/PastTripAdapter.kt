package com.example.showfadriverletest.ui.mytrip.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showfadriverletest.databinding.ItemPastTripDetailBinding
import com.example.showfadriverletest.response.pasttrip.TempResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PastTripAdapter(
    var context: Context,
    var drawerModel: ArrayList<TempResponse.DataItem>,
    var callback: (TempResponse.DataItem, timeStamp: String) -> Unit,
) :
    RecyclerView.Adapter<PastTripAdapter.MyViewHolder>() {

    var pickup = ""
    var dropoff = ""
    var referenceId = ""
    var pickupDateTime = ""
    var tripDuration = ""
    var distance = ""
    var arrivedTime = ""
    var tMinutes = 0L
    var tRemainingSeconds = 0L
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPastTripDetailBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = drawerModel.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = drawerModel[position]
        if (data.tripDuration != "" && data.tripDuration != null) {
            val (minutes, remainingSeconds) = convertSecondsStringToMinutesAndSeconds(data.tripDuration)
            tMinutes = minutes
            tRemainingSeconds = remainingSeconds
        } else {
            tMinutes = 0L
            tRemainingSeconds=0L
        }
        pickup = data.pickupLocation.toString()
        dropoff = data.dropoffLocation.toString()
        referenceId = data.id.toString()
        pickupDateTime = data.pickupDateTime.toString()
        tripDuration = data.bookingTime.toString()
        distance = data.distance.toString()
        arrivedTime = data.arrivedTime.toString()
        if (data.status.toString() == context.getString(com.example.showfadriverletest.R.string.completed)) {
            holder.binding.tvStatus.setTextColor(context.getColor(com.example.showfadriverletest.R.color.colorGreen))
        } else if (data.status.toString() == context.getString(com.example.showfadriverletest.R.string.pending)) {
            holder.binding.tvStatus.setTextColor(context.getColor(com.example.showfadriverletest.R.color.appPickColor))
        } else {
            holder.binding.tvStatus.setTextColor(context.getColor(com.example.showfadriverletest.R.color.red_dark))
        }

        holder.binding.tvStatus.text = data.status
        holder.binding.pickUpDropOff.tvPickupLocation.text = pickup
        holder.binding.pickUpDropOff.tvDropOffLocation.text = dropoff
        holder.binding.tvTripId.text = "ID:".plus(referenceId)
        holder.binding.tvDateTime.text = pickupDateTime
        holder.binding.layoutTrip.tvTripDuration.text = tMinutes.toString().plus("m" + " $tRemainingSeconds"+ "s")
        holder.binding.layoutTrip.tvGrandTotal.text = distance.plus(" KM")

        holder.itemView.setOnClickListener {
            callback.invoke(
                data,
                tMinutes.toString() + "m".plus(" :" + " $tRemainingSeconds" + "s")
            )
        }
    }

    inner class MyViewHolder(var binding: ItemPastTripDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun convertTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun convertSecondsStringToMinutesAndSeconds(secondsString: String): Pair<Long, Long> {
        val seconds = secondsString.toLong()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return Pair(minutes, remainingSeconds)
    }
}
