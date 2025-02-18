package com.spendsages.walletwatch.main

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.spendsages.walletwatch.Entry
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.getEntries
import com.spendsages.walletwatch.sortByDateAscending
import com.spendsages.walletwatch.sortByDateDescending
import com.spendsages.walletwatch.sortByPriceAscending
import com.spendsages.walletwatch.sortByPriceDescending
import org.w3c.dom.Document
import java.text.DecimalFormat
import java.util.Locale

/* This class provides support for the scrollable RecyclerView cardRecycler. */
class RecyclerAdapter(private var categories: Array<String>) :
    /* Maintain a list of all raw (unfiltered and unsorted) entries from the XML data file. */
    private lateinit var entriesRaw : MutableList<Entry>
    /* Maintain a list of filtered and sorted entries.
    * that are currently displayed in the RecyclerView.
    * This member variable is directly linked to the
    * RecyclerView items and item count. */
    private var entries = mutableListOf<Entry>()

    private var selectListener: OnClickListener? = null
    private var editListener: OnClickListener? = null

    /* Purpose: Sets the listener for an individual EntryViewHolder's checkbox.
    *
    * Parameters: listener represents the OnClickListener of a specific EntryViewHolder checkbox.
    *
    * Returns: Nothing. */
    fun setSelectListener(listener: OnClickListener) {
        this.selectListener = listener
    }

    /* Purpose: Sets the listener for an individual EntryViewHolder's edit button.
    *
    * Parameters: listener represents the OnClickListener of a specific EntryViewHolder edit button.
    *
    * Returns: Nothing. */
    fun setEditListener(listener: OnClickListener) {
        this.editListener = listener
    }

    /* Purpose: Filters the list of entries down to a specific category.
    *
    * Parameters: categoryLabel represents the label of the target category to filter by.
    *
    * Returns: Nothing. */
    fun filterEntries(categoryLabel : String) {
        /* Do nothing if the user has not added any entries yet whatsoever. */
        if (entriesRaw.isNotEmpty()) {
            if (categoryLabel == "All") {
                /* By default, do not filter out any entries. */
                entries = entriesRaw
            }
            else {
                /* Empty out list of filtered entries. */
                entries = ArrayList()
                /* Fill in list with all entries that match target category filter. */
                for (entry in entriesRaw) {
                    if (entry.category == categoryLabel) {
                        entries.add(entry)
                    }
                }
            }
        }
    }

    /* Purpose: Controller method that sorts the list of entries.
    *
    * Parameters: position represents the position of the sortingSpinner Spinbox.
    *
    * Returns: Nothing. */
    fun sortEntries(position : Int) {
        /* Do nothing if the user has not added any entries yet whatsoever. */
        if (entries.isNotEmpty()) {
            entries = when (position) {
                /* Sort by date from oldest to newest. */
                1 -> sortByDateAscending(entries)
                /* Sort by price from highest to lowest. */
                2 -> sortByPriceDescending(entries)
                /* Sort by price from lowest to highest. */
                3 -> sortByPriceAscending(entries)
                /* Sort by date from newest to oldest (default). */
                else -> sortByDateDescending(entries)
            }
        }
    }

    /* Purpose: Update the list of entries maintained in the background.
    *
    * Parameters: doc represents the Document of the XML data file.
    *             category represents the nullable string of the category to filter down to.
    *             sort represents the nullable sorting algorithm choice integer to sort by.
    *
    * Returns: Nothing. */
    fun updateData(doc: Document, category: String, sort: Int) {
        /* Retrieve the raw list of all entries in the XML data file. */
        entriesRaw = getEntries(doc)

        /* Filter the sorted entries list as necessary.
        * If we filter first, then sorting will be faster. */
        filterEntries(category)

        /* Sort the filtered entries list.
        * Filtering first helps the sorting algorithms go faster. */
        sortEntries(sort)
    }

    /* Purpose: Getter/Accessor that returns the total number of filtered entries to display.
    *
    * Parameters: None
    *
    * Returns: The number of filtered entries. */
    override fun getItemCount(): Int {
        return entries.size
    }

    /* Purpose: Public helper method that will find the index/position of the Entry
    * that has a matching id string.
    *
    * Parameters: id represents the numerical id of the target element.
    *
    * Returns: An integer representing the index/position of the target element or
    * -1 if the target element id was not found. */
    fun findEntryById(id : String) : Int {
        return if (entries.isNotEmpty()) {
            entries.indexOfFirst{ it.id == id }
        }
        else {
            -1
        }
    }

    /* Purpose: Controller method that sets the selection state of a specific Entry.
    *
    * Parameters: id represents the identifier string of the Entry
    * state represents the bool value of whether or not the Entry is selected (true)
    *
    * Returns: Nothing. */
    fun setEntryCheckBox(id : String, state : Boolean) {
        for (entry in entriesRaw) {
            if (entry.id == id) {
                entry.selected = state
            }
        }
    }

    /* This Internal Class is an object that represents an individual expense entry card,
    * which has a delete checkbox, displayed dollar amount, possibly a description of the purchase,
    * the date of purchase, the category in which the expense falls under, and an edit button.
    * Upon instantiation, the card is immediately added to the RecyclerView cardRecycler. */
    class EntryViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteCheckbox: AppCompatCheckBox = itemView.findViewById(R.id.deleteCheckbox)
        var amount: TextView = itemView.findViewById(R.id.amountText)
        var description: TextView = itemView.findViewById(R.id.descriptionText)
        var date: TextView = itemView.findViewById(R.id.dateText)
        var category: TextView = itemView.findViewById(R.id.categoryText)
        val editButton: AppCompatImageButton = itemView.findViewById(R.id.editButton)
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
            /* Format the purchase price amount to 2 decimals and
            * prepend with a dollar sign and a space with thousand-separator commas. */
            val amountText =  "$ " +
                    DecimalFormat("#,##0.00").format(entries[i].amount)

            entryViewHolder.amount.text = amountText

            entryViewHolder.description.text = entries[i].description

            /* Extract the date from the timestamp member of the entry. */
            entryViewHolder.date.text = SimpleDateFormat(
                "M/d/yyyy", Locale.US).format(SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).parse(
                entries[i].timestamp.toString().substring(0, 10)))

            var categoryLabel = entries[i].category
            /* Slice label to be at most 10 characters long. */
            if (categoryLabel.length > 10) {
                categoryLabel = categoryLabel.substring(0, 9) + "."
            }
            entryViewHolder.category.text = categoryLabel

            /* Add a unique content description for the checkbox. */
            entryViewHolder.deleteCheckbox.contentDescription =
                "Select Expense " + entryViewHolder.description.text + " " +
                        entryViewHolder.category.text + " $amountText " + entryViewHolder.date.text

            /* Add a unique content description for the edit button. */
            entryViewHolder.editButton.contentDescription =
                "Edit Expense " + entryViewHolder.description.text + " " +
                        entryViewHolder.category.text + " $amountText " + entryViewHolder.date.text

            entryViewHolder.deleteCheckbox.isChecked = entries[i].selected

            entryViewHolder.deleteCheckbox.setOnClickListener {
                selectListener?.onButtonClick(entries[i], entryViewHolder)
            }

            entryViewHolder.editButton.setOnClickListener {
                editListener?.onButtonClick(entries[i], entryViewHolder)
            }
        }
    }
}

interface OnClickListener {
    fun onButtonClick(entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder)
}