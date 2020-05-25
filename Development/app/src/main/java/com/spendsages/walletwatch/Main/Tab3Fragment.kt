package com.spendsages.walletwatch

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.layout_card.view.*
import me.abhinay.input.CurrencyEditText
import me.abhinay.input.CurrencySymbols
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab3Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab3Fragment : Fragment() {
    private lateinit var rootView : View
    private lateinit var main : MainActivity
    private lateinit var model : SharedViewModel

    private lateinit var categories : MutableList<String?>

    private lateinit var recycler : RecyclerView
    private lateinit var adapterRecycler : RecyclerAdapter

    /* List of entries selected for deletion. */
    private val selectedEntries = mutableListOf<String>()

    private lateinit var deleteButton : Button

    private lateinit var spinSorting : Spinner

    private lateinit var spinFiltering : Spinner

    /* Edit Entry window private member variables. */
    private lateinit var editWindow : ConstraintLayout

    private lateinit var entryID : String

    private val originalInputs = Array<String?>(4) { null }
    private val changedInputs = Array<String?>(4) { null }

    private lateinit var amountInput : CurrencyEditText
    private var validAmount = false

    private lateinit var descriptionInput : TextInputEditText

    private lateinit var dateInput : EditText
    private var validDate = true
    private lateinit var dateSelector : CalendarView
    private lateinit var dateOverlay : View
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

    /* Purpose: Controller method that will update the RecyclerView after the back-end sorts
    * the list of entries.
    *
    * Parameters: position represents the position of the sortingSpinner Spinbox.
    *
    * Returns: Nothing. */
    fun sortEntries(position : Int) {
        when (position) {
            1 -> adapterRecycler.entries = sortByDateAscending(adapterRecycler.entries)
            2 -> adapterRecycler.entries = sortByPriceDescending(adapterRecycler.entries)
            3 -> adapterRecycler.entries = sortByPriceAscending(adapterRecycler.entries)
            else -> adapterRecycler.entries = sortByDateDescending(adapterRecycler.entries)
        }
        /* Update the model, so that the changes are immediately displayed in the app. */
        adapterRecycler.notifyDataSetChanged()
    }

    private fun checkChanges() {
        for (i in 0..3) {
            if(changedInputs[i] != originalInputs[i]) {
                toggleButton(saveButton, true)
                return
            }
        }
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
            editWindow.visibility = View.VISIBLE

        }
        else {
            editWindow.visibility = View.GONE
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
        }
        else {
            dateSelector.visibility = View.GONE
            dateOverlay.visibility = View.GONE
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
            /* Replace all non-numeric characters in date with slashes.
            * Consecutive non-numeric characters will be replaced with a single dash. */
            val dateParsed = userDateFormat.parse(
                dateInput.text.toString().replace(Regex("[^0-9]+"), "/"))
            /* Check if date is in MM/dd/yyyy format. */
            (dateParsed != null && !dateParsed.after(modelDateFormat.parse(today)))
        }
        catch (_ : Exception) {
            false
        }

        return validDate
    }

    /* Purpose: Controller method that retrieves and validates user input,
    * then calls DataManager method editEntry to modify entry in local repo
    * and closes the Edit Entry window.
    *
    * Parameters: None
    *
    * Returns: Nothing. */
    private fun submitEdit() {
        /* Retrieve user inputs and convert each to string */
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()
        /* Convert date input into "yyyy-MM-dd" format. */
        val date = modelDateFormat.format(userDateFormat.parse(dateInput.text.toString())!!)
        val category = selectedCategory + 1

        /* Add the entry to the local repo. */
        DataManager.editEntry(model.get(), entryID, amount, description, date, category.toString())

        /* Update the data model. */
        model.save(main)

        /* Display the Toast message "Expense Modified". */
        success.show()

        /* Disable save button. */
        toggleButton(saveButton, false)

        /* Close the edit window. */
        toggleEditWindow(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tab3, container, false)
        main = activity as MainActivity
        model = main.model

        /* Setup the RecyclerView, which will dynamically load entry cards as the user scrolls. */
        recycler = rootView.findViewById(R.id.cardRecycler)
        /* Set the RecyclerView to have a vertical layout. */
        recycler.layoutManager = LinearLayoutManager(context)
        /* Connect the RecyclerView to the data model using the RecyclerAdapter. */
        adapterRecycler = RecyclerAdapter(model.get())
        /* Create a listener for the checkbox of each entry. */
        adapterRecycler.setSelectListener(
            object : OnClickListener {
                override fun onButtonClick(
                    entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder) {
                    /* Check if the checkbox is now checked. */
                    if (viewHolder.itemView.deleteCheckbox.isChecked) {
                        /* Add the entry id to the list of selected entries for deletion. */
                        selectedEntries.add(entry.id)
                    }
                    /* Otherwise, the checkbox is now unchecked. */
                    else {
                        /* Remove the entry id from the list of selected entries for deletion. */
                        selectedEntries.remove(entry.id)
                    }

                    /* Toggle the delete button depending on whether or not
                    * any entries are selected. */
                    when (selectedEntries.size) {
                        1 -> toggleButton(deleteButton, true)
                        0 -> toggleButton(deleteButton, false)
                    }
                }
            }
        )
        /* Create a listener for the edit button of each entry. */
        adapterRecycler.setEditListener(
            object : OnClickListener {
                override fun onButtonClick(
                    entry: Entry, viewHolder: RecyclerAdapter.EntryViewHolder) {
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

                    toggleButton(saveButton, false)
                    toggleButton(deleteButton, false)
                    /* Display the Edit Entry window. */
                    toggleEditWindow(true)
                }
            }
        )
        /* Attach the adapter to the RecyclerView. */
        recycler.adapter = adapterRecycler

        /* Initially set the "Delete Selected" button to be disabled and greyed-out. */
        deleteButton = rootView.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.isClickable = false
        deleteButton.alpha = 0.5F

        deleteButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val dialogClickListener: DialogInterface.OnClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                /* TODO (SPEN-32): If the user taps "Yes", then call the back-end function that
                                *   deletes a given list of selected entries.
                                *   Then disable the "Delete Selected" button.
                                *   Save the changes using: model.save(main)
                                *   The deleted entries should now no longer appear in Tab 3 whatsoever.
                                *   If the user taps "No", then simply close the Confirmation alert. */
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                dialog.dismiss()
                            }
                        }
                    }
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    var message = "Are you sure you want to delete " +
                            DecimalFormat("#,###").format(selectedEntries.size.toString()) +
                            " expense"
                    if(selectedEntries.size > 1) {
                        message += "s"
                    }
                    message += "? This cannot be undone."
                    builder.setMessage(message)
                        .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }
        })


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
        spinFiltering.adapter = ArrayAdapter<String?>(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            DataManager.getCategories(model.get()))
        /* Set the listener that will filter the entries down
        * to only those of the category chosen. If the user taps "All", then clear any filtering. */
        spinFiltering.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                when (position) {
                    0 -> adapterRecycler.entries = adapterRecycler.entriesRaw
                    else -> adapterRecycler.filter(spinFiltering.selectedItem.toString())
                }
                sortEntries(spinSorting.selectedItemPosition)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        /* Edit Entry window code. */
        editWindow = rootView.findViewById(R.id.editWindow)
        /* Initially hide the Edit Entry window. */
        toggleEditWindow(false)

        descriptionInput = rootView.findViewById(R.id.descriptionFieldEdit)

        descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the amount input since only the date was just changed. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                changedInputs[1] = s.toString()
                checkChanges()
            }

            override fun afterTextChanged(s: Editable) {}
        })

        /* Set Toast to "Expense Modified".
        * Ignore the warning since the Toast is shown in submitEdit. */
        success = Toast.makeText(context, R.string.changedEntryString, Toast.LENGTH_LONG)
        /* Center the "Expense Modified" Toast and position it at the top. */
        success.setGravity(Gravity.TOP + Gravity.CENTER_HORIZONTAL, 0, 0)

        /* Populate the Radio Button Group of category radio buttons. */
        categoryGroup = rootView.findViewById(R.id.categoryGroup)
        categoryGroup.setOnCheckedChangeListener { group, checkedId ->
            when(categoryGroup.checkedRadioButtonId) {
                R.id.category1ButtonEdit -> {
                    selectedCategory = 0
                }
                R.id.category2ButtonEdit -> {
                    selectedCategory = 1
                }
                R.id.category3ButtonEdit -> {
                    selectedCategory = 2
                }
            }
            changedInputs[3] = selectedCategory.toString()
            checkChanges()
        }
        categoryButtons[0] = rootView.findViewById(R.id.category1ButtonEdit)
        categoryButtons[1] = rootView.findViewById(R.id.category2ButtonEdit)
        categoryButtons[2] = rootView.findViewById(R.id.category3ButtonEdit)
        /* Retrieve the labels for each category. */
        categories = DataManager.getCategories(model.get())
        /* Set the category label and submit listener for each corresponding category button. */
        for ((index, button) in categoryButtons.withIndex()) {
            button?.text = categories[index + 1]
        }

        /* Set the cancel button to hide the Edit Entry window. */
        cancelButton = rootView.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            toggleEditWindow(false)
            if(selectedEntries.size > 0) {
                toggleButton(deleteButton, true)
            }

        }

        /* Set the save button to call the submitEdit function. */
        saveButton = rootView.findViewById(R.id.saveEditsButton)

        saveButton.setOnClickListener {
            submitEdit()
            if(selectedEntries.size > 0) {
                toggleButton(deleteButton, true)
            }
        }

        amountInput = rootView.findViewById(R.id.amountFieldEdit)
        /* Use the dollar sign "$". */
        amountInput.setCurrency(CurrencySymbols.USA)
        /* Add a space after the dollar sign. */
        amountInput.setSpacing(true)
        /* Set listener to enable category buttons if both inputs are valid. */
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the date input since only the amount was just changed. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validateAmountInput() && validDate) {
                    changedInputs[0] = s.toString()
                    checkChanges()
                }
                else {
                    toggleButton(saveButton, false)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        dateInput = rootView.findViewById(R.id.dateFieldEdit)
        /* The date should be initially set to the current date in the "MM/dd/yyyy" format. */
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))
        /* Set listener to enable category buttons if both inputs are valid. */
        dateInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the amount input since only the date was just changed. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validAmount && validateDateInput()) {
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

        /* Force both date formatters to require strict pattern matching. */
        modelDateFormat.isLenient = false
        userDateFormat.isLenient = false

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
            toggleButton(cancelButton, true)
            toggleButton(saveButton, validAmount && validDate)
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

            /* Open CalendarView. */
            toggleDateSelector(true)
        }

        rootView.setOnTouchListener { _: View, _: MotionEvent ->
            /* Hide the keyboard. */
            (main.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(amountInput.windowToken, 0)
        }

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab3Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab3Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
