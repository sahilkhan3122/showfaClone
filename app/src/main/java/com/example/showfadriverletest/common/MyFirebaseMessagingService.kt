package com.example.showfadriverletest.common

import android.Manifest
import android.R
import android.R.id.message
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.showfadriverletest.base.BaseActivity.Companion.deviceToken
import com.example.showfadriverletest.pref.MyDataStore
import com.example.showfadriverletest.ui.chat.ChatActivity
import com.example.showfadriverletest.ui.home.MapsActivity
import com.example.showfadriverletest.ui.mytrip.MyTripActivity
import com.example.showfadriverletest.ui.savingwallet.SavingWalletActivity
import com.example.showfadriverletest.ui.splash.SplashActivity
import com.example.showfadriverletest.ui.wallet.WalletActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Random

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    val TAG = "MyFirebaseMessagingService"
    private var random: Random? = null
    private var intent: Intent? = null
    var id = ""
    var m = 0
    private var activity: Activity? = null
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e(TAG, "onNewToken: $token")
        deviceToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e(
            TAG,
            "onMessageReceived() DATA:- ${remoteMessage.data}"
        )

        if (remoteMessage.data.isNotEmpty()) {
            val remoteMessageData = remoteMessage.data
            val message: String? = remoteMessageData["body"]
            val title: String? = remoteMessageData["title"]
            var type: String? = remoteMessageData["type"]
            val sound: String? = remoteMessageData["sound"]
            val response: String? = remoteMessageData["data"]
            if (type.equals("Activity Start")) {
                type = "activity_start";
            }
            sendNotification(remoteMessage.data)


            Log.e(TAG, "type : $type")
        }
    }

    private fun sendNotification(remoteMessageData: Map<String, String>) {
        val message: String? = remoteMessageData["body"]
        val title: String? = remoteMessageData["title"]
        val type: String? = remoteMessageData["type"]
        val sound: String? = remoteMessageData["sound"]
        val response: String? = remoteMessageData["data"]
        val intent: Intent?

        when (type) {
            "booking_chat" -> {
              /*  if (ChatActivity.Chat_IsVisible) {
                    ChatActivity.AddChatLine(
                        cha,
                        sender_type,
                        receiver_type,
                        message,
                        created_at
                    )
                } else {
                    Drawer_Activity.isAppTerminate = true
                    createNotification(title, message, type, m, imageUrl)
                }*/
                intent = Intent(this, ChatActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("data", response)
                //  startActivity(intent)
            }

            "trip_complete" -> {
                intent = Intent(this, MapsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                // startActivity(intent)
            }

            "receive_money" -> {
                intent = Intent(this, WalletActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                //startActivity(intent)
            }

            "add_money" -> {
                intent = Intent(this, SavingWalletActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            "send_money" -> {
                intent = Intent(this, WalletActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                // startActivity(intent)
            }

            "new_book_later" -> {
                intent = Intent(this, MyTripActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                // startActivity(intent)
            }

            "canceled_trip" -> {
                intent = Intent(this, MapsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                // startActivity(intent)
            }

            "vehicle_update" -> {
                intent = Intent(applicationContext, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                // startActivity(intent)
            }

            "logout" -> {
                intent = Intent(applicationContext, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                GlobalScope.launch {
                    MyDataStore(applicationContext).logoutId()
                    Constants.Login = ""
                    Constants.Register = ""
                    activity?.overridePendingTransition(
                        com.example.showfadriverletest.R.anim.exit_to_left,
                        com.example.showfadriverletest.R.anim.enter_from_right
                    )
                    startActivity(intent)
                }
            }

            else -> {
                intent = Intent(this, SplashActivity::class.java)
            }
        }
        var pendingIntent: PendingIntent? = null
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        createNotificationAccountVerify(pendingIntent, response, sound, type, title, message)
    }

    private fun createNotificationAccountVerify(
        intent: PendingIntent?,
        response: String?,
        sound: String?,
        type: String?,
        title: String?,
        message: String?,
    ) {
        val notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "All Notifications"
            val channelName: CharSequence = "All Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(notificationChannel)
            val notification: Notification = Notification.Builder(this)
                .setSmallIcon(com.example.showfadriverletest.R.mipmap.ic_launcher_round) //                   .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        com.example.showfadriverletest.R.mipmap.ic_launcher_round
                    )
                )
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                .setSound(notificationSoundURI)
                .setContentIntent(intent)
                .setGroupSummary(true)
                .setGroup("KEY_NOTIFICATION_GROUP")
                .setChannelId(channelId)
                .build()
            notificationManager.notify(m, notification)
        } else {
            val mNotificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this)
                .setSmallIcon(com.example.showfadriverletest.R.mipmap.ic_launcher_round) //                    .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        com.example.showfadriverletest.R.mipmap.ic_launcher_round
                    )
                )
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                .setSound(notificationSoundURI)
                .setContentIntent(intent)
                .setGroupSummary(true)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .setGroup("KEY_NOTIFICATION_GROUP")
            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notificationManager.notify(m, mNotificationBuilder.build())
        }
    }
}
