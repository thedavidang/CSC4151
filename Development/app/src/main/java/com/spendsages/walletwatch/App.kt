package com.spendsages.walletwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/* This class provides support for live data updating.
* By allowing fragments to share the same view model,
* they can all reference the same data,
* even as changes occur on that data. */
class ViewModelFactory() : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            val key = "SharedViewModel"
            return if(hashMapViewModel.containsKey(key)){
                getViewModel(key) as T
            } else {
                addViewModel(key, SharedViewModel())
                getViewModel(key) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        val hashMapViewModel = HashMap<String, ViewModel>()
        fun addViewModel(key: String, viewModel: ViewModel) {
            hashMapViewModel[key] = viewModel
        }

        fun getViewModel(key: String): ViewModel? {
            return hashMapViewModel[key]
        }
    }
}