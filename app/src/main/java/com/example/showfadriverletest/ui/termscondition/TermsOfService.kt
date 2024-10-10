package com.example.showfadriverletest.ui.termscondition

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.showfadriverletest.R
import com.example.showfadriverletest.common.ApiConstant
import com.example.showfadriverletest.common.ApiConstant.privacyPolicyUrl
import com.example.showfadriverletest.common.ApiConstant.termConditionUrl
import com.example.showfadriverletest.databinding.ActivityTermsOfServiceBinding

class TermsOfService : AppCompatActivity() {
    private lateinit var binding: ActivityTermsOfServiceBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_of_service)
        binding.apply {

            webView.webViewClient = WebViewClient()
            // this will load the url of the website
            if (ApiConstant.ISCHECKED_TERM_OF_SERVICE) {
                webView.loadUrl(termConditionUrl)
            } else {
                webView.loadUrl(termConditionUrl)
            }

            if (ApiConstant.ISCHECKED_PRIVACY_POLICY) {
                webView.loadUrl(privacyPolicyUrl)
            } else {
                webView.loadUrl(privacyPolicyUrl)
            }
            // this will enable the javascript settings, it can also allow xss vulnerabilities
            webView.settings.javaScriptEnabled = true
            // if you want to enable zoom feature
            webView.settings.setSupportZoom(true)
        }
    }
}