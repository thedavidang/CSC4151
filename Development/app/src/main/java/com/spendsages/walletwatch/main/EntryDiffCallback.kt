package com.spendsages.walletwatch.main

import androidx.recyclerview.widget.DiffUtil
import com.spendsages.walletwatch.Entry

class EntryDiffCallback: DiffUtil.ItemCallback<Entry>() {
    override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return (oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
        return (oldItem == newItem)
    }
}