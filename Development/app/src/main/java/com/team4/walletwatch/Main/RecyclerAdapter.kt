package com.team4.walletwatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Document
import java.text.DecimalFormat

class RecyclerAdapter(doc: Document) : RecyclerView.Adapter<RecyclerAdapter.EntryViewHolder?>() {
    var entriesRaw = getEntries(doc)
    var entries = sortByDateDescending(entriesRaw)

    override fun getItemCount(): Int {
        if (entries != null) {
            return entries!!.size
        }
        return 0
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): EntryViewHolder {
        return EntryViewHolder(LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_card, viewGroup, false))
    }

    override fun onBindViewHolder(entryViewHolder: EntryViewHolder, i: Int) {
        if (itemCount > 0) {
            val amountText =  "$ " + DecimalFormat("0.00").format(entries!![i].amount)
            entryViewHolder.amount.text = amountText
            entryViewHolder.description.text = entries!![i].description
            entryViewHolder.date.text = entries!![i].date.toString()
            entryViewHolder.category.text = entries!![i].category
        }
    }

    fun filter(text : String) {
        if (entriesRaw != null) {
            if (entries != null) {
                entries = ArrayList()
                for (entry in entriesRaw!!) {
                    if (entry.category == text) {
                        entries!!.add(entry)
                    }
                }
            }
        }
    }

    class EntryViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var amount: TextView = itemView.findViewById(R.id.amountText)
        var description: TextView = itemView.findViewById(R.id.descriptionText)
        var date: TextView = itemView.findViewById(R.id.dateText)
        var category: TextView = itemView.findViewById(R.id.categoryText)
    }
}