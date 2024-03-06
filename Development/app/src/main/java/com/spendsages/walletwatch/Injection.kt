package com.spendsages.walletwatch

import android.content.Context

/* This class provides support for injecting a shared view model into
* an app activity, which allows live data updating. */
object Injection {
    fun provideViewModelFactory(@Suppress("UNUSED_PARAMETER")context: Context): ViewModelFactory {
        return ViewModelFactory()
    }
}