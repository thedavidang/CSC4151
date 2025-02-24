package com.spendsages.walletwatch.main

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.spendsages.walletwatch.DataManager
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.SharedViewModel
import com.spendsages.walletwatch.databinding.FragmentTab1Binding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [Tab1Fragment] constructor method to
 * create an instance of this fragment.
 */
class Tab1Fragment: Fragment() {
    private var _binding: FragmentTab1Binding? = null
    private val binding get() = _binding!!

    private lateinit var main: MainActivity
    private lateinit var model: SharedViewModel

    private lateinit var amountInput: EditText
    private var validAmount: Boolean = false
    private lateinit var invalidAmount: ImageView
    private var amountInputEditedAtLeastOnce: Boolean = false

    private lateinit var descriptionInput: TextInputEditText

    private lateinit var dateInput: EditText
    private var validDate: Boolean = true
    private lateinit var dateSelector: CalendarView
    private lateinit var dateOverlay: View
    private lateinit var invalidDate: ImageView
    private lateinit var cancelDate: Button
    private val modelDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val userDateFormat = SimpleDateFormat("M/d/yyyy", Locale.US)
    private val today = LocalDate.now().toString()

    private lateinit var dateButton: ImageButton

    private val categoryButtons = arrayOfNulls<Button?>(3)

    private lateinit var success: Toast

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
    private fun toggleCategoryButtons(isEnabled: Boolean) {
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
    private fun validateAmountInput(): Boolean {
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
    * and is not an impossible or future date. The CalendarView UI only supports
    * dates starting on January 1st, 1900 AD, so we also have to reject user input
    * that attempts to enter a year of 1899 or older.
    *
    * Parameters: None.
    *
    * Returns: True if date field is both in valid format and possible. */
    private fun validateDateInput(): Boolean {
        validDate = try {
            val dateText = dateInput.text.toString()
            /* Replace all non-numeric characters in date with slashes.
            * Consecutive non-numeric characters will be replaced with a single dash. */
            val dateParsed = userDateFormat.parse(dateText)
            /* Check if date is in M/d/yyyy format, not in the future,
            * and not too far in the past. */
            (dateParsed != null && !dateParsed.after(modelDateFormat.parse(today)) &&
                    !dateParsed.before(modelDateFormat.parse("1900-01-01")) &&
                    dateText.matches(
                        Regex("(0?[1-9]|1[0-2])/(0?[1-9]|[12][0-9]|3[01])/[0-9]+")))
        }
        catch (_: Exception) {
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
    * then calls DataManager method addEntry to store entry in XML data file
    * and resets the Tab 1 screen.
    *
    * Parameters: category is an integer that represents the number of the
    * category button that was selected.
    *
    * Returns: Nothing. */
    private fun submitEntry(category: Int) {
        /* Retrieve user inputs and convert each to string */
        val amount = amountInput.text.toString()
        /* Trim off leading and trailing whitespace and truncate multiple whitespaces in
        * between words into a single space each from user input in description textbox. */
        val description = descriptionInput.text.toString().trim().replace(
            Regex("\\s+"), " "
        )
        /* Convert date input into "yyyy-MM-dd" format. */
        val date = modelDateFormat.format(userDateFormat.parse(dateInput.text.toString())!!)

        /* Add the entry to the XML data file. */
        DataManager.addEntry(model.get(), amount, description, date, category.toString())

        /* Update the data model. */
        model.save()

        /* Display the Toast message "Expense Added". */
        success.show()

        /* Reset description input back to empty string. */
        descriptionInput.setText("")

        /* Reset date input back to today's date. */
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))

        /* Forcibly disable the category submit buttons. */
        toggleCategoryButtons(false)

        /* Reset the amount input back to the default amount of zero. */
        amountInput.setText(R.string.amountHintString)
        /* Ensure that the cursor is placed at the far right end of the amount input textbox. */
        amountInput.setSelection(resources.getString(R.string.amountHintString).length)
        /* Override the fact that the invalid amount red 'X' technically should
        * appear being that the amount "$ 0.00" is in truth invalid.
        * We want to forcibly hide the invalid amount red 'X', so that
        * users have a better experience, since they won't be seeing a
        * big red 'X' every time they launch the app. */
        invalidAmount.visibility = View.GONE
        amountInputEditedAtLeastOnce = false

        /* Reopen the numpad for next entry to add. */
        main.showKeyboard(amountInput)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTab1Binding.inflate(inflater, container, false)
        val rootView = binding.root
        main = requireActivity() as MainActivity
        model = main.model

        descriptionInput = rootView.findViewById(R.id.descriptionField)
        /* Set listener on Description textbox, so that we can check if the user
        * is writing a description but forgot to enter in a valid amount input first. */
        descriptionInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                /* Check if the user completely forgot to
                * type something in the amount input textbox first. */
                if (!amountInputEditedAtLeastOnce) {
                    /* If the amount input is invalid,
                    * this helper method call will display the invalid amount red 'X'. */
                    validateAmountInput()
                }
            }
        })

        /* Set Toast to "Expense Added". */
        success = Toast.makeText(context, R.string.addedEntryString, Toast.LENGTH_LONG)

        /* Populate the fixed array of category buttons. */
        categoryButtons[0] = rootView.findViewById(R.id.category1Button)
        categoryButtons[1] = rootView.findViewById(R.id.category2Button)
        categoryButtons[2] = rootView.findViewById(R.id.category3Button)
        /* The category buttons should initially be disabled. */
        toggleCategoryButtons(false)
        /* Setup the submit listener for each corresponding category button. */
        for ((index, button) in categoryButtons.withIndex()) {
            button?.setOnClickListener {
                submitEntry(index + 1)
            }
        }

        amountInput = rootView.findViewById(R.id.amountField)
        amountInput.setText(R.string.amountHintString)
        amountInput.setSelection(resources.getString(R.string.amountHintString).length)
        /* Set listener to enable category buttons if both inputs are valid. */
        amountInput.addTextChangedListener(object: TextWatcher {
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

                toggleCategoryButtons(validateAmountInput() && validDate)
                /* The user just made an edit in the amount input, so make certain
                * that the flag is set to true. This is so listeners for other inputs
                * no longer have to check if the amount input invalid red 'x' needs
                * to be displayed. The amount input is zero "$ 0.00" at app launch,
                * which is technically an invalid input, but showing a red 'x' every time
                * the user opens the app is a poor user experience. Therefore, we
                * intentionally hide the red 'X' until the user types an invalid amount
                * input OR forgets the amount input textbox entirely by skipping ahead
                * to either the description input, date input, or date selector. */
                amountInputEditedAtLeastOnce = true
            }
        })
        /* Set listener to directly move focus when tapping the Next key on the numpad. */
        amountInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                /* Explicitly set focus to the Description field. */
                descriptionInput.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        invalidAmount = rootView.findViewById(R.id.invalidAmount)
        invalidAmount.visibility = View.GONE

        dateInput = rootView.findViewById(R.id.dateField)
        /* The date should be initially set to the current date in the "MM/dd/yyyy" format. */
        dateInput.setText(userDateFormat.format(modelDateFormat.parse(today)!!))
        /* Set listener to enable category buttons if both inputs are valid. */
        dateInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (validateDateInput()) {
                    /* Change the selected date in the CalenderView
                    * to the valid date input that the user just typed. */
                    dateSelector.setDate(userDateFormat.parse(dateInput.text.toString())!!.time,
                        true, true)
                }

                /* Check if the user completely forgot to
                * type something in the amount input textbox first. */
                if (!amountInputEditedAtLeastOnce) {
                    /* If the amount input is invalid,
                    * this helper method call will display the invalid amount red 'X'. */
                    validateAmountInput()
                }

                /* Re-enable category submit buttons,
                * but only if both the date and amount inputs are valid. */
                toggleCategoryButtons(validDate && validAmount)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        invalidDate = rootView.findViewById(R.id.invalidDate)
        invalidDate.visibility = View.GONE

        /* Force both date formatters to require strict pattern matching. */
        modelDateFormat.isLenient = false
        userDateFormat.isLenient = false

        /* The Cancel button will close the date selector without changing the date input. */
        cancelDate = rootView.findViewById(R.id.cancelDateButton)
        cancelDate.visibility = View.GONE
        cancelDate.setOnClickListener {
            toggleDateSelector(false)

            /* Check if the user completely forgot to
            * type something in the amount input textbox first. */
            if (!amountInputEditedAtLeastOnce) {
                /* If the amount input is invalid,
                * this helper method call will display the invalid amount red 'X'. */
                validateAmountInput()
            }

            /* Re-enable category submit buttons,
            * but only if both the date and amount inputs are valid. */
            toggleCategoryButtons(validDate && validAmount)
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

            /* Check if the user completely forgot to
            * type something in the amount input textbox first. */
            if (!amountInputEditedAtLeastOnce) {
                /* If the amount input is invalid,
                * this helper method call will display the invalid amount red 'X'. */
                validateAmountInput()
            }

            /* Re-enable category submit buttons,
            * but only if both the date and amount inputs are valid. */
            toggleCategoryButtons(validateDateInput() && validAmount)
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

        @Suppress("ClickableViewAccessibility")
        rootView.setOnTouchListener { _: View, _: MotionEvent ->
            /* Hide the keyboard. */
            (main.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(amountInput.windowToken, 0)
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        /* Only refresh the category button labels
        * if the user actually changed a category label
        * in the SettingsActivity. */
        if (model.getTabCategoriesNeedRefresh(0)) {
            /* Refresh the category label for each corresponding category button. */
            for ((index, button) in categoryButtons.withIndex()) {
                button?.text = model.getCategories()[index + 1]
            }
            /* Reset the tab's model boolean. */
            model.resetTabCategoriesNeedRefresh(0)
        }

        if (main.appLaunched) {
            main.showKeyboard(amountInput)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
