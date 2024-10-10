package com.example.showfadriverletest.util

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.example.showfadriverletest.R
import com.google.android.material.snackbar.Snackbar

class SnackbarUtils(
    private val viewById: View,
    private val aText: String,

    ) {
    fun snackieBar(): Snackbar {
        val snackie = Snackbar.make(viewById, aText, Snackbar.LENGTH_LONG)
            .setAction("Dismiss") {}
        snackie.setActionTextColor(ContextCompat.getColor(viewById.context, R.color.white))
        snackie.view.setBackgroundColor(ContextCompat.getColor(viewById.context, R.color.black))
        val snackbarView = snackie.view
        val startEndPadding = viewById.resources.getDimensionPixelSize(R.dimen._20dp)
        // Set padding to the Snackbar layout
        snackbarView.setPaddingRelative(20, 0, 20, 10)
        val params = snackie.view.layoutParams
        if (params is CoordinatorLayout.LayoutParams) {
            params.gravity = Gravity.BOTTOM
            params.leftMargin = startEndPadding
            params.rightMargin = startEndPadding
        } else if (params is FrameLayout.LayoutParams) {
            params.gravity = Gravity.BOTTOM
            params.leftMargin = startEndPadding
            params.rightMargin = startEndPadding
        }
        snackbarView.layoutParams = params

        val textView =
            snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(ContextCompat.getColor(viewById.context, R.color.white))
        textView.maxLines = 4
        /*val startEndPadding = viewById.resources.getDimensionPixelSize(R.dimen._20dp)
        textView.setPaddingRelative(startEndPadding, 0, startEndPadding, 20)*/
        snackie.show()

        return snackie
    }
}
