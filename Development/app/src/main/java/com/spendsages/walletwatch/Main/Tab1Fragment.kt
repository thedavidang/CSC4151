package com.spendsages.walletwatch

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.spendsages.walletwatch.databinding.FragmentTab1Binding
import com.cottacush.android.currencyedittext.CurrencyEditText
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Tab1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Tab1Fragment : Fragment() {
    private var _binding: FragmentTab1Binding? = null
    private val binding get() = _binding!!

    private lateinit var main : MainActivity
    private lateinit var model : SharedViewModel

    private lateinit var categories : MutableList<String?>

    private lateinit var amountInput : CurrencyEditText
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

    private val categoryButtons = Array<Button?>(3) { null }

    private lateinit var success : Toast

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

    /* Purpose: Controller method that disables and greys-out category buttons or
    * enables and reveals category buttons.
    *
    * Parameters: isEnabled represents a Boolean of whether or not to enable the category buttons.
    *
    * Returns: Nothing. */
    private fun toggleCategoryButtons(isEnabled : Boolean) {
        for (button in categoryButtons) {
            if (isEnabled) {
                button?.isEnabled = true
                button?.isClickable = true
                /* Set opacity to 100 % */
                button?.alpha = 1.0F
            }
            else {
                button?.isEnabled = false
                button?.isClickable = false
                /* Set opacity to 50 % */
                button?.alpha = 0.5F
            }
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

        if (input != "$ 0.00") {
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
    * then calls DataManager method addEntry to store entry in local repo
    * and resets the Tab 1 screen.
    *
    * Parameters: category is an integer that represents the number of the
    * category button that was selected.
    *
    * Returns: Nothing. */
    private fun submitEntry(category : Int) {
        /* Retrieve user inputs and convert each to string */
        val amount = amountInput.text.toString()
        val description = descriptionInput.text.toString()
        /* Convert date input into "yyyy-MM-dd" format. */
        val date = modelDateFormat.format(userDateFormat.parse(dateInput.text.toString())!!)

        /* Add the entry to the local repo. */
        DataManager.addEntry(model.get(), amount, description, date, category.toString())

        /* Update the data model. */
        model.save(main)

        /* Display the Toast message "Expense Added". */
        success.show()

        /* Reset screen. */
        amountInput.setText("")
        descriptionInput.setText("")
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))
        toggleCategoryButtons(false)
        invalidAmount.visibility = View.GONE

        /* Reopen the numpad for next entry to add. */
        main.showKeyboard(amountInput)

        /* Reset CalendarView to current date. */
        dateSelector.date = modelDateFormat.parse(today)!!.time
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTab1Binding.inflate(inflater, container, false)
        val rootView = binding.root
        main = activity as MainActivity
        model = main.model

        descriptionInput = rootView.findViewById(R.id.descriptionField)

        /* Set Toast to "Expense Added".
        * Ignore the warning since the Toast is shown in submitEntry. */
        success = Toast.makeText(context, R.string.addedEntryString, Toast.LENGTH_LONG)

        /* Populate the fixed array of category buttons. */
        categoryButtons[0] = rootView.findViewById(R.id.category1Button)
        categoryButtons[1] = rootView.findViewById(R.id.category2Button)
        categoryButtons[2] = rootView.findViewById(R.id.category3Button)
        /* The category buttons should initially be disabled. */
        toggleCategoryButtons(false)
        /* Retrieve the labels for each category. */
        categories = DataManager.getCategories(model.get())
        /* Set the category label and submit listener for each corresponding category button. */
        for ((index, button) in categoryButtons.withIndex()) {
            button?.text = categories[index + 1]
            button?.setOnClickListener {
                submitEntry(index + 1)
            }
        }

        amountInput = rootView.findViewById(R.id.amountField)
        /* Set focus to amountField and open the numpad. */
        amountInput.requestFocus()
        /* Use the dollar sign "$". */
        amountInput.setCurrencySymbol("$ ", useCurrencySymbolAsHint = true)
        /* Set listener to enable category buttons if both inputs are valid. */
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the date input since only the amount was just changed. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validateAmountInput() && validDate) {
                    toggleCategoryButtons(true)
                }
                else {
                    toggleCategoryButtons(false)
                }
        }

            override fun afterTextChanged(s: Editable) {}
        })

        invalidAmount = rootView.findViewById(R.id.invalidAmount)
        invalidAmount.visibility = View.GONE

        dateInput = rootView.findViewById(R.id.dateField)
        /* The date should be initially set to the current date in the "MM/dd/yyyy" format. */
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))
        /* Set listener to enable category buttons if both inputs are valid. */
        dateInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            /* Do not bother checking the amount input since only the date was just changed. */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validateDateInput() && validAmount) {
                    toggleCategoryButtons(true)
                    dateSelector.setDate(userDateFormat.parse(dateInput.text.toString())!!.time,
                        true, true)
                }
                else {
                    toggleCategoryButtons(false)
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
            toggleCategoryButtons(validAmount && validDate)
        }

        dateSelector = rootView.findViewById(R.id.dateSelector)
        /* Restrict the user from selecting a future date in the CalendarView. */
        dateSelector.maxDate = modelDateFormat.parse(today)!!.time
        /* Set the date selected listener to hide the CalendarView and its background and
        * replace the date input with the selected date. */
        dateSelector.setOnDateChangeListener { _: CalendarView, year: Int, month: Int, day: Int ->
            toggleDateSelector(false)
            val dateString = (month + 1).toString() + "/$day/$year"
            dateInput.setText(dateString)

            /* Re-enable category buttons, if necessary. */
            toggleCategoryButtons(validAmount && validDate)
        }

        dateOverlay = rootView.findViewById(R.id.dateOverlay)

        /* The CalendarView and its background overlay should be hidden initially. */
        toggleDateSelector(false)

        dateButton = rootView.findViewById(R.id.dateButton)
        /* Set the listener to reveal the CalendarView and its background. */
        dateButton.setOnClickListener {
            /* Hide the keyboard. */
            (main.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(amountInput.windowToken, 0)

            /* Temporarily disable category buttons. */
            toggleCategoryButtons(false)

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

    override fun onResume() {
        super.onResume()
        if (main.launched) {
            amountInput.requestFocus()
            main.showKeyboard(amountInput)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tab1Fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tab1Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
