package com.smartv2v.app

import android.app.Application
import com.smartv2v.app.data.local.TokenManager
import com.smartv2v.app.data.remote.ApiClient

class SmartV2VApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
        ApiClient.init(TokenManager.getServerBaseUrl())
    }
}
