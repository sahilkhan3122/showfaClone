package com.example.showfadriverletest.ui.mytrip.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showfadriverletest.R
import com.example.showfadriverletest.databinding.ItemUpcomingTripBinding
import com.example.showfadriverletest.response.featuretrip.FeatureTripResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpcomingTripAdapter(
    var context: Context,
    var upcomingList: ArrayList<FeatureTripResponse.DataItem>,
    var callback: (FeatureTripResponse.DataItem,timeStamp:String) -> Unit,
) :
    RecyclerView.Adapter<UpcomingTripAdapter.MyViewHolder>() {

    var pickup = ""
    var dropoff = ""
    var referenceId = ""
    var pickupDateTime = ""
    var tripDuration = ""
    var arrivedTime = ""
    var tMinutes = 0L
    var tRemainingSeconds = 0L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var binding = ItemUpcomingTripBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int = upcomingList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var data = upcomingList[position]

        holder.binding.layoutTrip.tvTripDuration.setText(context.getString(R.string.estimate_time))
        holder.binding.layoutTrip.tvGrandTotal.setText(context.getString(R.string.estimate_km))
/*        var timeStamp = convertTimestampToTime(data.bookingTime!!.toLong())*/
        if (data.tripDuration != "" && data.tripDuration != null) {
            val (minutes, remainingSeconds) = convertSecondsStringToMinutesAndSeconds(data.tripDuration!!)
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
        arrivedTime = data.arrivedTime.toString()

        if (data.status.toString() == context.getString(R.string.completed)) {
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.colorGreen))
        } else if (data.status.toString() == context.getString(com.example.showfadriverletest.R.string.pending)) {
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.appPickColor))
        } else {
            holder.binding.tvStatus.setTextColor(context.getColor(R.color.red_dark))
        }
        holder.binding.tvStatus.text = data.status
        holder.binding.pickUpDropOff.tvPickupLocation.text = pickup
        holder.binding.pickUpDropOff.tvDropOffLocation.text = dropoff
        holder.binding.tvTripId.text = "ID: $referenceId"
        holder.binding.tvDateTime.text = pickupDateTime
        holder.binding.layoutTrip.tvTripDuration.text = tMinutes.toString().plus("m" + " $tRemainingSeconds"+ "s")
        holder.binding.layoutTrip.tvGrandTotal.text = arrivedTime
        holder.itemView.setOnClickListener {
            callback.invoke(data,tMinutes.toString() + "m".plus(" :" + " $tRemainingSeconds" + "s"))
        }
    }

    inner class MyViewHolder(var binding: ItemUpcomingTripBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun convertSecondsStringToMinutesAndSeconds(secondsString: String): Pair<Long, Long> {
        val seconds = secondsString.toLong()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return Pair(minutes, remainingSeconds)
    }
}
