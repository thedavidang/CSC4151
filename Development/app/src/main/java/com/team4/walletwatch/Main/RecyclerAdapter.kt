package com.team4.walletwatch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Document
import java.text.DecimalFormat

/* This class provides support for the scrollable RecyclerView cardRecycler. */
class RecyclerAdapter(doc: Document) : RecyclerView.Adapter<RecyclerAdapter.EntryViewHolder?>() {
    /* Retrieve the list of all entries in the local repo XML file. */
    var entriesRaw = getEntries(doc)
    /* Create a list of those entries that are sorted by date from newest to oldest. */
    var entries = sortByDateDescending(entriesRaw)

    /* Purpose: Getter/Accessor that returns the total number of  filtered entries to display.
    *
    * Parameters: None
    *
    * Returns: The number of filtered entries. */
    override fun getItemCount(): Int {
        if (entries != null) {
            return entries!!.size
        }
        return 0
    }

    /* Purpose: Filters the list of entries down to a specific category.
    *
    * Parameters: categoryLabel represents the label of the target category to filter by.
    *
    * Returns: Nothing. */
    fun filter(categoryLabel : String) {
        /* Do nothing if the user has not added any entries yet whatsoever. */
        if (entriesRaw != null) {
            /* Empty out list of entries to display */
            entries = ArrayList()
            /* Fill in list with all entries that are part of target category. */
            for (entry in entriesRaw!!) {
                if (entry.category == categoryLabel) {
                    entries!!.add(entry)
                }
            }
        }
    }

    /* This Internal Class is an object that represents an individual expense entry card,
    * which has a displayed dollar amount, possibly a description of the purchase,
    * the date of purchase, and the category in which the expense falls under.
    * Upon instantiation, the card is immediately added to the RecyclerView cardRecycler. */
    class EntryViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var amount: TextView = itemView.findViewById(R.id.amountText)
        var description: TextView = itemView.findViewById(R.id.descriptionText)
        var date: TextView = itemView.findViewById(R.id.dateText)
        var category: TextView = itemView.findViewById(R.id.categoryText)
    }

    /* Purpose: Creates an individual expense entry card using layout_card.xml.
    *
    * Parameters: viewGroup represents a ViewGroup object
    * i represents an integer of the index position of this entry in the list of entries
    *
    * Returns: An EntryViewHolder object, which is an individual expense entry card. */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): EntryViewHolder {
        return EntryViewHolder(LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_card, viewGroup, false))
    }

    /* Purpose: Fills in the contents of an individual expense entry card
    *
    * Parameters: entryViewHolder represents an individual expense entry card object
    * i represents an integer of the index position of this entry in the list of entries
    *
    * Returns: An EntryViewHolder object, which is an individual expense entry card. */
    override fun onBindViewHolder(entryViewHolder: EntryViewHolder, i: Int) {
        /* Confirm that there is at least one entry to display in the list of filtered entries. */
        if (itemCount > 0) {
            /* Format the purchase price amount  to 2 decimals and
            * prepend with a dollar sign and a space. */
            val amountText =  "$ " +
                    DecimalFormat("0.00").format(entries!![i].amount)
            entryViewHolder.amount.text = amountText
            entryViewHolder.description.text = entries!![i].description
            entryViewHolder.date.text = entries!![i].date.toString()
            entryViewHolder.category.text = entries!![i].category
        }
    }
}