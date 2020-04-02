package com.team4.walletwatch

import android.content.Context

object Injection {
    fun provideViewModelFactory(context: Context): ViewModelFactory {
        return ViewModelFactory()
    }
}