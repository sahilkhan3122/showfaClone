package com.example.showfadriverletest.di.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import com.example.showfadriverletest.common.ApiConstant
import dagger.hilt.android.HiltAndroidApp
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

@HiltAndroidApp
class MyApplication : Application() {
    private var mSocket: Socket? = null
    var activity: Activity? = null
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate() {

        super.onCreate()

        try {
            mSocket = IO.socket(ApiConstant.SocketUrl)

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

       /* ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    Log.e("data_information", "on create")
                }
                Lifecycle.Event.ON_START -> {
                    Log.e("data_information", "on start")
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.e("data_information", "on resume")
                    try {
                        stopService(Intent(applicationContext, FloatingViewService::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.e("data_information", "on pause")
                    try {
                        if (Constants.isSwitchOn) {
                            startService(Intent(applicationContext, FloatingViewService::class.java))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.e("data_information", "on stop")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.e("data_information", "on destroy")
                    try {
                        stopService(Intent(applicationContext, FloatingViewService::class.java))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Lifecycle.Event.ON_ANY -> {
                    Log.e("data_information", "on any")
                }
            }
        })*/
    }

    fun getMSocket(): Socket? {
        return mSocket
    }

    /*fun ShowDialog(context: Context?, title: String?, message: String?) {
        Log.e("call", "show dialog from application")
        val dialog = Dialog(context!!, 0)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_common_error)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        //lp.width = Comman.DEVICE_WIDTH;
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        val dialog_title = dialog.findViewById<TextView>(R.id.dialog_title)
        val dialog_message = dialog.findViewById<TextView>(R.id.dialog_message)
        val closeButton = dialog.findViewById<ImageView>(R.id.closeButton)
        val dialog_ok_layout = dialog.findViewById<LinearLayout>(R.id.dialog_ok_layout)
        dialog_title.text = title
        dialog_message.text = message
        closeButton.setOnClickListener { dialog.dismiss() }
        dialog_ok_layout.setOnClickListener { dialog.dismiss() }
        dialog.window!!.setBackgroundDrawableResource(R.color.colorPickTransparent)
        dialog.window!!.attributes = lp
        dialog.show()

        ErrorDialogClass errorDialogClass = new ErrorDialogClass(activity);
        errorDialogClass.showDialog(message, title);
    }


    fun getCurrentActivity(): Activity? {
        //Config.logger("call","getCurrentActivity = "+activity.getLocalClassName());
        return activity
    }

    fun setCurrentActivity(activity1: Activity) {
        //Config.logger("call","getCurrentActivity = "+activity1.getLocalClassName());
        activity = activity1
    }

    fun isEmailValid(input: CharSequence?): Boolean {
//        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
//        Matcher matcher = pattern.matcher(input);
//        return (!matcher.matches());
        return Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    fun getCurrentDate(): String? {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd, EEE")
        val str = dateFormat.format(Date())
        Log.e("strDate", "date = $str")
        return dateFormat.format(Date())
    }

    fun isOnline(context: Context): Boolean {
        val connectivity = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null) for (i in info.indices) if (info[i].state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocalManager().setLocale(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalManager().setLocale(this)
    }*/
}