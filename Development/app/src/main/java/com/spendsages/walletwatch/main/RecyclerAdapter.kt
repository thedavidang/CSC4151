package com.spendsages.walletwatch.main

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spendsages.walletwatch.Entry
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.getEntries
import org.w3c.dom.Document
import java.text.DecimalFormat
import java.util.Locale

/* This class provides support for the scrollable RecyclerView cardRecycler. */
class RecyclerAdapter(private var categories: Array<String>) :
    ListAdapter<Entry, RecyclerAdapter.EntryViewHolder>(EntryDiffCallback()) {
    /* Maintain a list of all raw (unfiltered and unsorted) entries from the XML data file. */
    private lateinit var entriesRaw: MutableList<Entry>

    private var selectListener: OnClickListener? = null
    private var editListener: OnClickListener? = null

    /* Purpose: Sets the listener for an individual EntryViewHolder's checkbox.
    *
    * Parameters: listener represents the OnClickListener of a specific EntryViewHolder checkbox.
    *
    * Returns: Nothing. */
    fun setSelectListener(listener: OnClickListener) {
        selectListener = listener
    }

    /* Purpose: Sets the listener for an individual EntryViewHolder's edit button.
    *
    * Parameters: listener represents the OnClickListener of a specific EntryViewHolder edit button.
    *
    * Returns: Nothing. */
    fun setEditListener(listener: OnClickListener) {
        editListener = listener
    }

    /* Purpose: Initializes the list of entries maintained in the background.
    *
    * Parameters: doc represents the Document of the XML data file.
    *
    * Returns: Nothing. */
    fun initializeData(doc: Document) {
        /* Retrieve the raw list of all entries in the XML data file. */
        entriesRaw = getEntries(doc)
    }

    /* Purpose: Getter/Accessor that returns the total number of filtered entries to display.
    *
    * Parameters: None
    *
    * Returns: The number of filtered entries. */
    override fun getItemCount(): Int {
        return currentList.size
    }

    /* Purpose: Sorts a given sequence of entries.
    *
    * Parameters: sort represents the selected sorting algorithm index.
    *
    * Returns: A sorted list of entries. */
    private fun Sequence<Entry>.sortEntries(sort: Int): List<Entry> {
        return asSequence().sortedWith(
                when (sort) {
                    /* Sort by date from oldest to newest. */
                    1 -> compareBy { it.timestamp }
                    /* Sort by price from highest to lowest. */
                    2 -> compareByDescending { it.amount }
                    /* Sort by price from lowest to highest. */
                    3 -> compareBy { it.amount }
                    /* Sort by date from newest to oldest (default). */
                    else -> compareByDescending { it.timestamp }
                }
            ).toList()
    }

    /* Purpose: Filter and sort a given sequence of entries.
    *
    * Parameters: filter represents the selected category filter index.
    *             sort represents the selected sorting algorithm index.
    *
    * Returns: A filtered and sorted list of entries. */
    private fun Sequence<Entry>.filterEntries(filter: Int, sort: Int): List<Entry> {
        val filteredList = when (filter) {
            /* All categories can be displayed, so do not filter. */
            0 -> asSequence()
            /* Filter down to the selected category. */
            else -> asSequence().filter { it.category == filter }
        }
        /* Sort the filtered list of entries. */
        return filteredList.sortEntries(sort)
    }

    /* Purpose: Sort the currently displayed entries.
    *
    * Parameters: recyclerView represents the RecyclerView UI element.
    *             sort represents the selected sorting algorithm index.
    *
    * Returns: Nothing. */
    fun submitSort(recyclerView: RecyclerView, sort: Int) {
        if (currentList.isNotEmpty()) {
            submitList(currentList.asSequence().sortEntries(sort)) {
                /* Jump back to the very top of the list. */
                recyclerView.scrollToPosition(0)
            }
        }
    }

    /* Purpose: Filter and sort the currently displayed entries.
    *
    * Parameters: recyclerView represents the RecyclerView UI element.
    *             filter represents the selected category filter index.
    *             sort represents the selected sorting algorithm index.
    *
    * Returns: Nothing. */
    fun submitFilter(recyclerView: RecyclerView, filter: Int, sort: Int) {
        if (entriesRaw.isNotEmpty()) {
            submitList(entriesRaw.asSequence().filterEntries(filter, sort)) {
                /* Jump back to the very top of the list. */
                recyclerView.scrollToPosition(0)
            }
        }
    }

    /* Purpose: Edits a given entry, then re-filters and re-sorts the list of entries.
    *
    * Parameters: filter represents the selected category filter index.
    *             sort represents the selected sorting algorithm index.
    *             id represents the Entry ID string to modify.
    *             fields represents the new values for each of the entry's fields.
    *
    * Returns: Nothing. */
    fun submitEdit(filter: Int, sort: Int, id: String, fields: (Entry) -> Entry) {
        val updatedList = currentList.map { entry ->
            if (entry.id == id) {
                fields(entry)
            }
            else {
                entry
            }
        }

        /* Edit raw list as well. */
        entriesRaw.map { entry ->
            if (entry.id == id) {
                fields(entry)
            }
            else {
                entry
            }
        }

        /* Filter and sort the updated list. */
        submitList(updatedList.asSequence().filterEntries(filter, sort))
    }

    /* Purpose: Deselects all entry checkboxes.
    *
    * Parameters: count represents the quantity of entries that were selected.
    *
    * Returns: Nothing. */
    fun submitDeselect(count: Int) {
        val updatedList = if (count < currentList.size / 2) {
            /* If only a few entries were selected, only deselect those. */
            currentList.map { if (it.selected) it.copy(selected = false) else it }
        }
        else {
            /* If more than half the list were selected, it's faster to just deselect everything. */
            currentList.map { it.copy(selected = false) }
        }

        submitList(updatedList)
    }

    /* Purpose: Deletes all selected entries.
    *
    * Parameters: selectedEntries represents a hash set of the ID strings of the selected entries.
    *
    * Returns: Nothing. */
    fun submitDelete(selectedEntries: HashSet<String>) {
        val updatedList = currentList.filterNot { it.id in selectedEntries }

        /* Remove from raw list as well. */
        entriesRaw = entriesRaw.filterNot { it.id in selectedEntries } as MutableList<Entry>

        submitList(updatedList)
    }

    /* Purpose: Updates the category label strings.
    *
    * Parameters: recyclerView represents the RecyclerView UI element.
    *             newCategories represents the list of updated category label strings.
    *
    * Returns: Nothing. */
    fun submitCategories(recyclerView: RecyclerView, newCategories: Array<String>) {
        /* Re-populate the member variable with the new category label strings. */
        categories = newCategories

        /* Create a new list with updated category names. */
        val updatedList = currentList.map { entry ->
            /* Ensure entries are considered "new" by DiffUtil. */
            entry.copy()
        }

        submitList(updatedList) {
            /* Jump back to the very top of the list. */
            recyclerView.scrollToPosition(0)
        }
    }

    /* This Internal Class is an object that represents an individual expense entry card,
    * which has a delete checkbox, displayed dollar amount, possibly a description of the purchase,
    * the date of purchase, the category in which the expense falls under, and an edit button.
    * Upon instantiation, the card is immediately added to the RecyclerView cardRecycler. */
    class EntryViewHolder internal constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        var deleteCheckbox: AppCompatCheckBox = itemView.findViewById(R.id.deleteCheckbox)
        var amount: TextView = itemView.findViewById(R.id.amountText)
        var description: TextView = itemView.findViewById(R.id.descriptionText)
        var date: TextView = itemView.findViewById(R.id.dateText)
        var category: TextView = itemView.findViewById(R.id.categoryText)
        val editButton: AppCompatImageButton = itemView.findViewById(R.id.editButton)

        /* Purpose: Binds an Entry object to an EntryViewHolder object.
        *
        * Parameters: entry represents an individual expense entry object.
        *             categoryLabel represents the category label string.
        *
        * Returns: Nothing. */
        fun bind(entry: Entry, categoryLabel: String) {
            /* Format the purchase price amount to 2 decimals and
            * prepend with a dollar sign and a space with thousand-separator commas. */
            val amountText =  "$ " + DecimalFormat("#,##0.00").format(entry.amount)
            amount.text = amountText

            description.text = entry.description

            /* Extract the date from the timestamp member of the entry. */
            date.text = SimpleDateFormat(
                "M/d/yyyy", Locale.US).format(SimpleDateFormat(
                "yyyy-MM-dd", Locale.US).parse(
                entry.timestamp.toString().substring(0, 10)))


            category.text = if (categoryLabel.length <= 10) {
                categoryLabel
            }
            else {
                /* Slice label to be at most 10 characters long. */
                categoryLabel.substring(0, 9) + "."
            }

            /* Add a unique content description for the checkbox. */
            deleteCheckbox.contentDescription = "Select Expense " + description.text + " " +
                        category.text + " $amountText " + date.text
            /* Set the selection state of the checkbox. */
            deleteCheckbox.isChecked = entry.selected

            /* Add a unique content description for the edit button. */
            editButton.contentDescription = "Edit Expense " + description.text + " " +
                        category.text + " $amountText " + date.text
        }
    }

    /* Purpose: Creates an individual expense entry card using layout_card.xml.
    *
    * Parameters: viewGroup represents a ViewGroup object.
    *             i represents the index position of this entry in the list of entries.
    *
    * Returns: An EntryViewHolder object, which is an individual expense entry card. */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): EntryViewHolder {
        return EntryViewHolder(LayoutInflater.from(viewGroup.context).inflate(
            R.layout.layout_card, viewGroup, false))
    }

    /* Purpose: Fills in the contents of an individual expense entry card.
    *
    * Parameters: entryViewHolder represents an individual expense entry card object.
    *             i represents the index position of this entry in the list of entries.
    *
    * Returns: An EntryViewHolder object, which is an individual expense entry card. */
    override fun onBindViewHolder(entryViewHolder: EntryViewHolder, i: Int) {
        /* Confirm that there is at least one entry to display in the list of filtered entries. */
        if (itemCount > 0) {
            val entry = getItem(i)
            entryViewHolder.bind(entry, categories[entry.category])

            entryViewHolder.deleteCheckbox.setOnClickListener {
                selectListener?.onButtonClick(entry, entryViewHolder)
            }

            entryViewHolder.editButton.setOnClickListener {
                editListener?.onButtonClick(entry, entryViewHolder)
            }
        }
    }
}

/* Purpose: Interface function that handles user taps of the pencil icon edit button.
*
* Parameters: entry represents an individual expense entry object.
*             viewHolder represents an individual expense entry card object.
*
* Returns: Nothing. */
interface OnClickListener {
    fun onButtonClick(entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder)
}