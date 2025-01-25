package com.spendsages.walletwatch.main

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.spendsages.walletwatch.DataManager
import com.spendsages.walletwatch.Entry
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.SharedViewModel
import com.spendsages.walletwatch.databinding.FragmentTab3Binding
import com.spendsages.walletwatch.sortByDateAscending
import com.spendsages.walletwatch.sortByDateDescending
import com.spendsages.walletwatch.sortByPriceAscending
import com.spendsages.walletwatch.sortByPriceDescending
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [Tab3Fragment] constructor method to
 * create an instance of this fragment.
 */
class Tab3Fragment : Fragment() {
    private var _binding: FragmentTab3Binding? = null
    private val binding get() = _binding!!

    private lateinit var main : MainActivity
    private lateinit var model : SharedViewModel

    private lateinit var categories : MutableList<String?>

    private lateinit var recycler : RecyclerView
    private lateinit var adapterRecycler : RecyclerAdapter

    /* List of entries selected for deletion. */
    private val selectedEntries = mutableListOf<String>()
    /* Total dollar sum of entries selected for deletion. */
    private var selectedSum : Double = 0.00

    private lateinit var deselectAllCheckBox : AppCompatCheckBox

    private lateinit var deleteButton : Button

    private lateinit var spinSorting : Spinner

    private lateinit var spinFiltering : Spinner

    /* Edit Entry window private member variables. */
    private lateinit var scroll : ScrollView
    private lateinit var editOverlay : View

    private lateinit var entryID : String
    private val originalInputs = Array<String?>(4) { null }
    private val changedInputs = Array<String?>(4) { null }

    private lateinit var amountInput : EditText
    private var validAmount = false
    private lateinit var invalidAmount : ImageView

    private lateinit var descriptionInput : TextInputEditText

    private lateinit var dateInput : EditText
    private var validDate = true
    private lateinit var dateSelector : CalendarView
    private lateinit var dateOverlay : View
    private lateinit var invalidDate : ImageView
    private lateinit var cancelDate : Button
    private val modelDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val userDateFormat = SimpleDateFormat("M/d/yyyy", Locale.US)
    private val today = LocalDate.now().toString()

    private lateinit var dateButton : ImageButton

    private lateinit var categoryGroup : RadioGroup
    private var selectedCategory: Int = 0
    private val categoryButtons = Array<RadioButton?>(3) { null }

    private lateinit var cancelButton : Button
    private lateinit var saveButton : Button

    private lateinit var success : Toast
    private lateinit var deselect : Toast
    private lateinit var delete : Toast

    /* Purpose: Helper method that will find the index/position of the Entry
    * that has a matching id string.
    *
    * Parameters: id represents the numerical id of the target element.
    *
    * Returns: An integer representing the index/position of the target element or
    * -1 if the target element id was not found. */
    private fun findEntryById(id : String) : Int {
        return if (!adapterRecycler.entries.isNullOrEmpty()) {
            adapterRecycler.entries!!.indexOfFirst{
                it.id == id
            }
        } else {
            -1
        }
    }

    /* Purpose: Helper method that will display the quantity of entries
    * selected for deletion and the total dollar sum of those same entries
    * on the deselect all checkbox.
    *
    * Parameters: None.
    *
    * Returns: None. */
    private fun updateDeselectAllCheckBoxText() {
        /* Initialize the text content string with the quantity of entries
        * selected for deletion, a newline, a dollar sign, and a space. */
        var textContent : String = selectedEntries.size.toString() + " Selected\n$ "

        if (selectedSum >= 0.00) {
            /* Format the total dollar sum to 2 decimals with thousand-separator commas. */
            val textSum : String = DecimalFormat("#,##0.00").format(selectedSum)
            /* The deselect all checkbox only has room for 20 characters on the second line.
            * This means we can only show total dollar sums that are less than one trillion.
            * If we use the maximum possible number as an example:
            * $ 999,999,999,999.99         = Highest number that fits within 20 characters
            * 12345678901234567890         = Ones digit for the 1-based position of the character
            *          11111111112         = Tens digit for the 1 based position of the character
            * Therefore, we have to ensure that the sum is only 18 characters, being that
            * the first two character slots are reserved for the dollar sign and space. */
            textContent += if (textSum.length <= 18) {
                /* Concatenate the sum to the text content string. */
                textSum
            }
            else {
                /* If the sum ended up getting too big, display the biggest number that fits. */
                "999,999,999,999.99"
            }
        }
        else {
            /* If the sum somehow ended up going negative, display a zero sum. */
            textContent += "0.00"
        }

        /* Finally, update the text content of the deselect all checkbox. */
        deselectAllCheckBox.text = textContent
    }

    /* Purpose: Controller method that will update the RecyclerView after the back-end sorts
    * the list of entries.
    *
    * Parameters: position represents the position of the sortingSpinner Spinbox.
    *
    * Returns: Nothing. */
    @Suppress("NotifyDataSetChanged")
    fun sortEntries(position : Int) {
        if (!adapterRecycler.entries.isNullOrEmpty()) {
            when (position) {
                1 -> adapterRecycler.entries = sortByDateAscending(adapterRecycler.entries)
                2 -> adapterRecycler.entries = sortByPriceDescending(adapterRecycler.entries)
                3 -> adapterRecycler.entries = sortByPriceAscending(adapterRecycler.entries)
                else -> adapterRecycler.entries = sortByDateDescending(adapterRecycler.entries)
            }
        }
        /* Update the model, so that the changes are immediately displayed in the app. */
        adapterRecycler.notifyDataSetChanged()
    }

    /* Purpose: Controller method that disables the Save Changes button
    * if no actual changes were made or enables the Save Changes button
    * if at least one actual change was made.
    *
    * Parameters: None.
    *
    * Returns: Nothing. */
    private fun checkChanges() {
        /* Iterate through and compare each input in the arrays. */
        for (i in 0..3) {
            /* If a difference was found, enable the Save Changes button. */
            if (changedInputs[i] != originalInputs[i]) {
                toggleButton(saveButton, true)
                return
            }
        }
        /* Otherwise, disable the Save Changes button since no actual changes were made. */
        toggleButton(saveButton, false)
    }

    /* Purpose: Controller method that disables and hides Edit Entry window or
    * enables and shows Edit Entry window.
    *
    * Parameters: isEnabled represents a Boolean of whether or not to enable the Edit Entry window.
    *
    * Returns: Nothing. */
    private fun toggleEditWindow(isEnabled: Boolean) {
        if (isEnabled) {
            scroll.visibility = View.VISIBLE
        }
        else {
            scroll.visibility = View.GONE
        }
    }

    /* Purpose: Controller method that disables and hides CalendarView dateSelector or
    * enables and shows CalendarView dateSelector.
    *
    * Parameters: isEnabled represents a Boolean of whether or not to enable the dateSelector.
    *
    * Returns: Nothing. */
    private fun toggleDateSelector(isEnabled: Boolean) {
        if (isEnabled) {
            dateSelector.visibility = View.VISIBLE
            dateOverlay.visibility = View.VISIBLE
            cancelDate.visibility = View.VISIBLE
        }
        else {
            dateSelector.visibility = View.GONE
            dateOverlay.visibility = View.GONE
            cancelDate.visibility = View.GONE
        }
    }

    /* Purpose: Controller method that disables and greys-out a button or
    * enables and reveals a button.
    *
    * Parameters: button represents the Button widget to toggle.
    * isEnabled represents a Boolean of whether or not to enable the button.
    *
    * Returns: Nothing. */
    private fun toggleButton(button : Button, isEnabled : Boolean) {
        if (isEnabled) {
            button.isEnabled = true
            button.isClickable = true
            /* Set opacity to 100 % */
            button.alpha = 1.0F
        }
        else {
            button.isEnabled = false
            button.isClickable = false
            /* Set opacity to 50 % */
            button.alpha = 0.5F
        }
    }

    /* Purpose: Validate amount EditText ensuring that field is not empty
    * and is not an amount of zero.
    *
    * Parameters: None.
    *
    * Returns: True if amount field is not empty and not zero. */
    private fun validateAmountInput() : Boolean {
        val input = amountInput.text.toString()
        validAmount = (input.isNotEmpty() && input != "$ 0.00")

        if (validAmount) {
            invalidAmount.visibility = View.GONE
        }
        else {
            invalidAmount.visibility = View.VISIBLE
        }

        return validAmount
    }

    /* Purpose: Validate date EditText ensuring that field is properly formatted
    * and is not an impossible or future date.
    *
    * Parameters: None.
    *
    * Returns: True if date field is both in valid format and possible. */
    private fun validateDateInput() : Boolean {
        validDate = try {
            val dateText = dateInput.text.toString()
            /* Replace all non-numeric characters in date with slashes.
            * Consecutive non-numeric characters will be replaced with a single dash. */
            val dateParsed = userDateFormat.parse(dateText)
            /* Check if date is in M/d/yyyy format. */
            (dateParsed != null && !dateParsed.after(modelDateFormat.parse(today)) &&
                    dateText.matches(
                        Regex("(0?[1-9]|1[0-2])/(0?[1-9]|[12][0-9]|3[01])/[0-9]+")))
        }
        catch (_ : Exception) {
            false
        }

        if (validDate) {
            invalidDate.visibility = View.GONE
        }
        else {
            invalidDate.visibility = View.VISIBLE
        }

        return validDate
    }

    /* Purpose: Controller method that retrieves and validates user input,
    * then calls DataManager method editEntry to modify entry in XML data file
    * and closes the Edit Entry window.
    *
    * Parameters: None
    *
    * Returns: Nothing. */
    private fun submitEdit() {
        val date = modelDateFormat.format(userDateFormat.parse(changedInputs[2]!!)!!)
        val category = (changedInputs[3]!!.toInt() + 1).toString()

        /* Add the entry to the XML data file. */
        DataManager.editEntry(
            model.get(), entryID,
            changedInputs[0]!!, changedInputs[1]!!, date, category
        )

        /* Update the data model. */
        model.save()

        /* Update the model, so that the changes are immediately displayed in the app. */
        adapterRecycler.updateData(model.get(), spinSorting.selectedItemPosition,
            spinFiltering.selectedItem.toString())
        adapterRecycler.notifyItemChanged(findEntryById(entryID))

        /* Display the Toast message "Expense Modified". */
        success.show()

        /* Disable save button. */
        toggleButton(saveButton, false)

        /* Close the edit window. */
        toggleEditWindow(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTab3Binding.inflate(inflater, container, false)
        val rootView = binding.root
        main = requireActivity() as MainActivity
        model = main.model

        /* Setup the RecyclerView, which will dynamically load entry cards as the user scrolls. */
        recycler = rootView.findViewById(R.id.cardRecycler)
        /* Set the RecyclerView to have a vertical layout. */
        recycler.layoutManager = LinearLayoutManager(context)
        /* Connect the RecyclerView to the data model using the RecyclerAdapter. */
        adapterRecycler = RecyclerAdapter(model.get())
        /* Attach the adapter to the RecyclerView. */
        recycler.adapter = adapterRecycler

        /* Create a listener for the checkbox of each entry. */
        adapterRecycler.setSelectListener(
            object : OnClickListener {
                override fun onButtonClick(
                    entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder
                ) {
                    /* Check if the checkbox is now checked. */
                    if (viewHolder.itemView.findViewById<AppCompatCheckBox>(
                            R.id.deleteCheckbox
                        ).isChecked) {
                        /* Set the entry's internal boolean to true. */
                        entry.selected = true
                        /* Add the entry id to the list of selected entries for deletion. */
                        selectedEntries.add(entry.id)
                        /* Increment the total dollar sum of entries selected for deletion. */
                        selectedSum += entry.amount
                    }
                    /* Otherwise, the checkbox is now unchecked. */
                    else {
                        /* Set the entry's internal boolean to false. */
                        entry.selected = false
                        /* Remove the entry id from the list of selected entries for deletion. */
                        selectedEntries.remove(entry.id)
                        /* Decrement the total dollar sum of entries selected for deletion. */
                        selectedSum -= entry.amount
                    }

                    /* Update the text of the deselectAllCheckbox to the
                    * new count (selectedEntries.size) and sum (selectedSum). */
                    updateDeselectAllCheckBoxText()

                    /* Toggle the deselect all checkbox and delete button
                    * depending on whether or not any entries are selected. */
                    when (selectedEntries.size) {
                        1 -> {
                            toggleButton(deselectAllCheckBox, true)
                            deselectAllCheckBox.isChecked = true
                            toggleButton(deleteButton, true)
                        }
                        0 -> {
                            toggleButton(deselectAllCheckBox, false)
                            deselectAllCheckBox.isChecked = false
                            toggleButton(deleteButton, false)
                        }
                    }
                }
            }
        )

        /* Create a listener for the edit button of each entry. */
        adapterRecycler.setEditListener(
            object : OnClickListener {
                override fun onButtonClick(
                    entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder
                ) {
                    /* Retrieve the id, amount, description, date, and category of the entry. */
                    entryID = entry.id
                    amountInput.setText(viewHolder.amount.text)
                    originalInputs[0] = viewHolder.amount.text.toString()
                    changedInputs[0] = viewHolder.amount.text.toString()

                    descriptionInput.setText(viewHolder.description.text)
                    originalInputs[1] = viewHolder.description.text.toString()
                    changedInputs[1] = viewHolder.description.text.toString()

                    dateInput.setText(viewHolder.date.text)
                    originalInputs[2] = viewHolder.date.text.toString()
                    changedInputs[2] = viewHolder.date.text.toString()
                    /* Set the CalendarView to the entry date. */
                    dateSelector.setDate(userDateFormat.parse(dateInput.text.toString())!!.time,
                        true, true)

                    /* Make the correct category RadioButton checked. */
                    val category = entryID.substring(2, 3).toInt() - 1
                    categoryGroup.check(category)
                    selectedCategory = category
                    categoryButtons[category]?.isChecked = true
                    originalInputs[3] = category.toString()
                    changedInputs[3] = category.toString()

                    /* Disable the Save Changes button since no changes have been made yet. */
                    toggleButton(saveButton, false)
                    /* Temporarily disable the deselect all checkbox as to hide it. */
                    toggleButton(deselectAllCheckBox, false)
                    /* Temporarily disable the Delete Selected button as to hide it. */
                    toggleButton(deleteButton, false)
                    /* Display the Edit Entry window. */
                    toggleEditWindow(true)
                }
            }
        )

        /* Set the possible options for the sorting Spinbox. */
        spinSorting = rootView.findViewById(R.id.sortingSpinner)
        spinSorting.adapter = ArrayAdapter(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.sortingOptions))
        /* Set the listener that will sort the entries in a new order
        * when a new option is selected. */
        spinSorting.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                sortEntries(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Set the possible options for the filtering Spinbox. */
        spinFiltering = rootView.findViewById(R.id.filteringSpinner)
        /* Set the listener that will filter the entries down
        * to only those of the category chosen. If the user taps "All", then clear any filtering. */
        spinFiltering.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { adapterRecycler.entries = adapterRecycler.entriesRaw }
                    else -> { adapterRecycler.filter(spinFiltering.selectedItem.toString()) }
                }
                sortEntries(spinSorting.selectedItemPosition)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Initially set the deselect all checkbox to be disabled and greyed-out. */
        deselectAllCheckBox = rootView.findViewById(R.id.deselectAllCheckBox)
        toggleButton(deselectAllCheckBox, false)
        /* Listener for the deselect all checkbox. */
        deselectAllCheckBox.setOnClickListener {
            val dialogClickListener: DialogInterface.OnClickListener =
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        /* If user taps "Yes", then uncheck all entry checkboxes,
                        * disable the deselect all checkbox, and disable the
                        * "Delete Selected" button. */
                        DialogInterface.BUTTON_POSITIVE -> {
                            for (entryId in selectedEntries) {
                                adapterRecycler.setEntryCheckBox(entryId, false)
                                val entryIndex = findEntryById(entryId)
                                adapterRecycler.notifyItemChanged(entryIndex)
                            }
                            /* Clear the array, so that it is empty. */
                            selectedEntries.clear()
                            selectedSum = 0.00
                            /* Immediately display the changes in the app. */
                            deselectAllCheckBox.text = getString(R.string.deselectAllString)
                            toggleButton(deselectAllCheckBox, false)
                            deselectAllCheckBox.isChecked = false
                            toggleButton(deleteButton, false)
                            deselect.show()
                        }
                        /* If the user taps "No", then simply close the confirmation alert. */
                        DialogInterface.BUTTON_NEGATIVE -> {
                            /* Since the user just tapped the deselect all checkbox a second ago,
                            * the deselect all checkbox no longer as a checkmark.
                            * We need to restore the checkmark being that user did not mean
                            * to deselect all, thus there are still entries selected that can be
                            * deselected later. */
                            deselectAllCheckBox.isChecked = true
                        }
                    }
                }
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            /* Create the message to display on the confirmation alert. */
            val message = "Are you sure you want to deselect all entries?"
            /* Display the confirmation alert. */
            builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        /* Initially set the "Delete Selected" button to be disabled and greyed-out. */
        deleteButton = rootView.findViewById(R.id.deleteButton)
        toggleButton(deleteButton, false)
        /* Listener for the Delete Selected button. */
        deleteButton.setOnClickListener {
            val dialogClickListener: DialogInterface.OnClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        /* If user taps "Yes", then call the back-end function,
                        * save the change, and disable the Delete Selected button. */
                        DialogInterface.BUTTON_POSITIVE -> {
                            DataManager.deleteEntries(model.get(), selectedEntries)
                            for (entryId in selectedEntries) {
                                adapterRecycler.notifyItemRemoved(findEntryById(entryId))
                            }
                            /* Clear the array, so that it is empty. */
                            selectedEntries.clear()
                            selectedSum = 0.00
                            model.save()
                            /* Update the model, so that the changes are
                            * immediately displayed in the app. */
                            adapterRecycler.updateData(model.get(),
                                spinSorting.selectedItemPosition,
                                spinFiltering.selectedItem.toString())
                            deselectAllCheckBox.text = getString(R.string.deselectAllString)
                            toggleButton(deselectAllCheckBox, false)
                            deselectAllCheckBox.isChecked = false
                            toggleButton(deleteButton, false)
                            delete.show()
                        }
                        /* If the user taps "No", then simply close the confirmation alert. */
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            /* Create the message to display on the confirmation alert. */
            var message = "Are you sure you want to delete " +
                    DecimalFormat("#,###").format(selectedEntries.size) + " expense"
            /* Check for plural expenses. */
            if (selectedEntries.size > 1) {
                message += "s"
            }
            message += "? This cannot be undone."
            /* Display the confirmation alert. */
            builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
        }

        /* Edit Entry window code. */
        scroll = rootView.findViewById(R.id.scroll)
        editOverlay = rootView.findViewById(R.id.editOverlay)
        /* Initially hide the Edit Entry window. */
        toggleEditWindow(false)

        descriptionInput = rootView.findViewById(R.id.descriptionFieldEdit)
        /* Set listener to check if Description changed. */
        descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                /* Trim off leading and trailing whitespace and truncate multiple whitespaces in
                * between words into a single space each from user input in description textbox. */
                changedInputs[1] = s.toString().trim().replace(
                    Regex("\\s+"), " "
                )
                /* Check if the trimmed Description is actually different
                * from the currently saved Description. */
                if (validAmount && validDate) {
                    checkChanges()
                }
            }
        })

        /* Set Toast to "Expense Modified". */
        success = Toast.makeText(context, R.string.changedEntryString, Toast.LENGTH_LONG)

        /* Set Toast to "Unchecked All Selected Entries". */
        deselect = Toast.makeText(context, R.string.deselectedEntriesString, Toast.LENGTH_LONG)

        /* Set Toast to "Selection Deleted". */
        delete = Toast.makeText(context, R.string.deletedEntriesString, Toast.LENGTH_LONG)

        /* Populate the Radio Button Group of category radio buttons. */
        categoryGroup = rootView.findViewById(R.id.categoryGroup)
        categoryGroup.setOnCheckedChangeListener { _, _ ->
            /* Select the correct category RadioButton. */
            when(categoryGroup.checkedRadioButtonId) {
                R.id.category1ButtonEdit -> { selectedCategory = 0 }
                R.id.category2ButtonEdit -> { selectedCategory = 1 }
                R.id.category3ButtonEdit -> { selectedCategory = 2 }
            }
            /* Check if an actual category change was made. */
            changedInputs[3] = selectedCategory.toString()
            if (validAmount && validDate) {
                checkChanges()
            }
        }
        categoryButtons[0] = rootView.findViewById(R.id.category1ButtonEdit)
        categoryButtons[1] = rootView.findViewById(R.id.category2ButtonEdit)
        categoryButtons[2] = rootView.findViewById(R.id.category3ButtonEdit)

        /* Set the cancel button to hide the Edit Entry window. */
        cancelButton = rootView.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            toggleEditWindow(false)
            if (selectedEntries.size > 0) {
                toggleButton(deselectAllCheckBox, true)
                deselectAllCheckBox.isChecked = true
                toggleButton(deleteButton, true)
            }
        }

        /* Set the save button to call the submitEdit function. */
        saveButton = rootView.findViewById(R.id.saveEditsButton)

        saveButton.setOnClickListener {
            submitEdit()

            if (selectedEntries.size > 0) {
                /* Update the text of the deselectAllCheckbox to the
                * new count (selectedEntries.size) and sum (selectedSum). */
                updateDeselectAllCheckBoxText()
                toggleButton(deselectAllCheckBox, true)
                deselectAllCheckBox.isChecked = true
                toggleButton(deleteButton, true)
            }
        }

        amountInput = rootView.findViewById(R.id.amountFieldEdit)
        /* Set listener to enable category buttons if both inputs are valid. */
        amountInput.addTextChangedListener(object : TextWatcher {
            /* Format the amount input into a defined, safe, and consistent format. */
            private val decimalFormat: DecimalFormat =
                NumberFormat.getCurrencyInstance() as DecimalFormat
            init {
                decimalFormat.applyPattern("$ ###,###,###,##0.00")
            }

            /* Extract the significant digits from tha amount input
            * and return a Long integer without the decimal point. */
            private fun parseAmount(text: String): Long {
                val digits = text.replace(Regex("[^0-9]"), "")
                return if (digits.isEmpty()) {
                    0L
                } else {
                    digits.toLong()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            /* Do not bother checking the date input since only the amount was just changed. */
            override fun afterTextChanged(s: Editable) {
                /* Forcibly format the amount input into the desired format. */
                val amount = parseAmount(s.toString())
                val formattedAmount = decimalFormat.format(amount / 100.00)

                /* Temporarily disable this text change listener to
                * prevent multiple triggering events be fired. */
                amountInput.removeTextChangedListener(this)
                /* Forcibly update the text displayed in the textbox. */
                amountInput.setText(formattedAmount)
                /* Set cursor position. */
                amountInput.setSelection(formattedAmount.length)
                /* Restore the text change listener. */
                amountInput.addTextChangedListener(this)

                if (validateAmountInput() && validDate) {
                    changedInputs[0] = formattedAmount
                    checkChanges()
                }
                else {
                    toggleButton(saveButton, false)
                }
            }
        })

        invalidAmount = rootView.findViewById(R.id.invalidAmount)
        invalidAmount.visibility = View.GONE

        dateInput = rootView.findViewById(R.id.dateFieldEdit)
        /* The date should be initially set to the current date in the "MM/dd/yyyy" format. */
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))
        /* Set listener to enable category buttons if both inputs are valid. */
        dateInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the amount input since only the date was just changed.
            * If valid, then check if an actual date change was made. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validateDateInput() && validAmount) {
                    changedInputs[2] = s.toString()
                    checkChanges()
                    dateSelector.setDate(userDateFormat.parse(dateInput.text.toString())!!.time,
                        true, true)
                }
                else {
                    toggleButton(saveButton, false)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        invalidDate = rootView.findViewById(R.id.invalidDate)
        invalidDate.visibility = View.GONE

        /* Force both date formatters to require strict pattern matching. */
        modelDateFormat.isLenient = false
        userDateFormat.isLenient = false

        /* The Cancel button will close the date selector. */
        cancelDate = rootView.findViewById(R.id.cancelDateButton)
        cancelDate.visibility = View.GONE
        cancelDate.setOnClickListener {
            toggleDateSelector(false)

            /* Re-enable category buttons, if necessary. */
            toggleButton(cancelButton, true)
            for (button in categoryButtons) {
                toggleButton(button!!, true)
            }
            toggleButton(saveButton, validAmount && validDate)
            checkChanges()
        }

        dateSelector = rootView.findViewById(R.id.dateSelectorEdit)
        /* Restrict the user from selecting a future date in the CalendarView. */
        dateSelector.maxDate = modelDateFormat.parse(today)!!.time
        /* Set the date selected listener to hide the CalendarView and its background and
        * replace the date input with the selected date. */
        dateSelector.setOnDateChangeListener { _: CalendarView, year: Int, month: Int, day: Int ->
            toggleDateSelector(false)
            val dateString = (month + 1).toString() + "/$day/$year"
            dateInput.setText(dateString)

            /* Re-enable category buttons, if necessary. */
            for (button in categoryButtons) {
                toggleButton(button!!, true)
            }
            toggleButton(cancelButton, true)
            toggleButton(saveButton, validAmount && validDate)
            checkChanges()
        }

        dateOverlay = rootView.findViewById(R.id.dateOverlayEdit)

        /* The CalendarView and its background overlay should be hidden initially. */
        toggleDateSelector(false)

        dateButton = rootView.findViewById(R.id.dateButtonEdit)
        /* Set the listener to reveal the CalendarView and its background. */
        dateButton.setOnClickListener {
            /* Hide the keyboard. */
            (main.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(amountInput.windowToken, 0)

            /* Temporarily disable buttons. */
            toggleButton(cancelButton, false)
            toggleButton(saveButton, false)
            for (button in categoryButtons) {
                toggleButton(button!!, false)
            }

            /* Open CalendarView. */
            toggleDateSelector(true)
        }

        @Suppress("ClickableViewAccessibility")
        editOverlay.setOnTouchListener { _: View, _: MotionEvent ->
            /* Hide the keyboard. */
            (main.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(amountInput.windowToken, 0)
        }

        return rootView
    }

    @Suppress("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Observe the LiveData objects from SharedViewModel. */
        model.getLive().observe(viewLifecycleOwner) { doc ->
            /* Refresh the categories. */
            categories = DataManager.getCategories(doc)
            /* Refresh the category label for each category button. */
            for ((index, button) in categoryButtons.withIndex()) {
                button?.text = categories[index + 1]
            }

            /* Refresh the possible options for the filtering Spinbox. */
            spinFiltering.adapter = ArrayAdapter<String?>(requireActivity(),
                R.layout.support_simple_spinner_dropdown_item, categories)

            adapterRecycler.updateData(model.get(), spinSorting.selectedItemPosition,
                spinFiltering.selectedItem.toString())
            adapterRecycler.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
