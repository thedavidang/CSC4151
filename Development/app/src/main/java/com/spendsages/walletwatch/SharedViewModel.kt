package com.spendsages.walletwatch

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.w3c.dom.Document

/* This class provides support for sharing live data amongst different views. */
class SharedViewModel(context: Context) : ViewModel() {
    private val repository: DataRepository = DataRepository(context)
    private var tabCategoriesNeedRefresh = Array(3){ true }

    fun getLive(): MutableLiveData<Document> {
        return repository.doc
    }

    fun get(): Document {
        return repository.doc.value!!
    }

    fun getString(doc: Document): CharSequence {
        return repository.docString(doc)
    }

    fun save() {
        repository.save()
    }

    fun notifyTabCategoriesNeedRefresh() {
        tabCategoriesNeedRefresh.fill(true)
    }

    fun getTabCategoriesNeedRefresh(index : Int) : Boolean {
        return tabCategoriesNeedRefresh[index]
    }

    fun resetTabCategoriesNeedRefresh(index : Int) {
        tabCategoriesNeedRefresh[index] = false
    }
}