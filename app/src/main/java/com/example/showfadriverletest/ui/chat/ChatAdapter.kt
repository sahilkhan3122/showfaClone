package com.example.showfadriverletest.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showfadriverletest.base.BaseViewHolder
import com.example.showfadriverletest.databinding.ItemChatListBinding
import com.example.showfadriverletest.databinding.ItemChatReceiveBinding
import com.example.showfadriverletest.response.chat.ChatHistoryResponse
import com.example.showfadriverletest.response.chat.ChatResponse
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date


class ChatAdapter(
    var context: Context, var chatHistoryModel: ArrayList<ChatResponse.DataItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        val sendMessageView = 2
        val receiveMessageView = 1
    }

    inner class SendMessageHolder(var binding: ItemChatListBinding) : BaseViewHolder(binding.root) {
        private val mBinding = binding

        override fun onBind(position: Int) {
            val list = chatHistoryModel[position]
            mBinding.txtSendMessage.text = list.message.toString()
        }
    }

    inner class ReceiveMessageHolder(var binding: ItemChatReceiveBinding) :
        BaseViewHolder(binding.root) {

        private val mBinding = binding

        override fun onBind(position: Int) {
            var list = chatHistoryModel[position]
            mBinding.txtReceivedMessage.text = list.message.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return chatHistoryModel[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            sendMessageView -> {
                return SendMessageHolder(
                    ItemChatListBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
             }

            receiveMessageView -> {
                return ReceiveMessageHolder(
                    ItemChatReceiveBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> return SendMessageHolder(
                ItemChatListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (chatHistoryModel[position].viewType) {
            sendMessageView -> {
                (holder as SendMessageHolder).onBind(position)
                holder.binding.txtSendMessage.text = chatHistoryModel[position].message
                holder.binding.txtSendTime.text =
                    convertToTime(chatHistoryModel[position].createdAt)
            }

            receiveMessageView -> {
                (holder as ReceiveMessageHolder).onBind(position)
                holder.binding.txtReceivedMessage.text = chatHistoryModel[position].message
                holder.binding.txtReceivedTime.text =
                    convertToTime(chatHistoryModel[position].createdAt)
            }
            else -> {
                (holder as SendMessageHolder).onBind(position)
            }
        }
    }

    override fun getItemCount() = chatHistoryModel.size

    fun convertToTime(timestamp: String?): String? {
        val inputPattern = "yyyy-MM-dd HH:mm:ss"
        val outputPattern = "h:mm a"
        @SuppressLint("SimpleDateFormat") val inputFormat = SimpleDateFormat(inputPattern)
        @SuppressLint("SimpleDateFormat") val outputFormat = SimpleDateFormat(outputPattern)
        var date: Date? = null
        var str: String? = null
        try {
            date = inputFormat.parse(timestamp)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str
    }
}
