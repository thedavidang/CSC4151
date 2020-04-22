package com.team4.walletwatch

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import me.abhinay.input.CurrencyEditText
import me.abhinay.input.CurrencySymbols
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
    private lateinit var rootView : View
    private lateinit var main : MainActivity
    private lateinit var model : SharedViewModel

    private lateinit var amountInput : CurrencyEditText

    private lateinit var descriptionInput : EditText

    private lateinit var dateInput : EditText
    private lateinit var dateSelector : CalendarView
    private lateinit var dateOverlay : View
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
        }
        else {
            dateSelector.visibility = View.GONE
            dateOverlay.visibility = View.GONE
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
        /* Replace all non-numeric characters in date with dashes.
        * Consecutive non-numeric characters will be replaced with a single dash. */
        var date = dateInput.text.toString().replace(
            Regex("[^0-9]+"), "-")

        /* Remove leading and trailing dashes from date */
        if (date.startsWith("-")) {
            date = date.removePrefix("-")
        }
        if (date.endsWith("-")) {
            date = date.removeSuffix("-")
        }
        /* Check if date is valid format and convert into yyyy-MM-dd format. */
        date = try {
            /* Attempt to read date as yyyy-MM-dd format. */
            var dateParsed = sdf.parse(date)
            /* If date is not in yyyy-MM-dd format, then try MM-dd-yyyy format. */
            if (dateParsed == null ||
                (dateParsed.year + 1900).toString() != date.substring(0, 4)) {
                dateParsed = SimpleDateFormat("MM-dd-yyyy", Locale.US).parse(date)
                /* If date is not in MM-dd-yyyy format, then try dd-MM-yyyy format. */
                if (dateParsed == null  ||
                    (dateParsed.year + 1900).toString() != date.substring(6)) {
                    dateParsed = SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(date)
                    /* If date is not in dd-MM-yyyy format, then assume it must be
                    * an invalid format and forcibly set date to current date. */
                    if (dateParsed == null  ||
                        (dateParsed.year + 1900).toString() != date.substring(6)) {
                        dateParsed = sdf.parse(today)!!
                    }
                }
            }
            /* Convert date to yyyy-MM-dd format. */
            sdf.format(dateParsed)
        } catch (e: Exception) {
            /* If any exception occurs, then assume it must be
            * an invalid format and forcibly set date to current date. */
            today
        }

        /* Add the entry to the local repo. */
        DataManager.addEntry(model.get(), amount, description, date, category.toString())

        /* Update the data model. */
        model.save(main)

        /* Display the Toast message "Entry Added". */
        success.show()

        /* Reset Tab 1 screen. */
        amountInput.setText("")
        descriptionInput.setText("")
        dateInput.setText(today)
        toggleCategoryButtons(false)

        main.showKeyboard(amountInput)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tab1, container, false)
        main = activity as MainActivity
        model = main.model

        descriptionInput = rootView.findViewById(R.id.descriptionField)

        success = Toast.makeText(context, R.string.addedEntryString, Toast.LENGTH_LONG)
        success.setGravity(Gravity.TOP + Gravity.CENTER_HORIZONTAL, 0, 0)

        categoryButtons[0] = rootView.findViewById(R.id.category1Button)
        categoryButtons[1] = rootView.findViewById(R.id.category2Button)
        categoryButtons[2] = rootView.findViewById(R.id.category3Button)
        toggleCategoryButtons(false)
        val categories = DataManager.getCategories(model.get())
        for ((index, button) in categoryButtons.withIndex()) {
            button?.text = categories[index + 1]
            button?.setOnClickListener { submitEntry(index + 1) }
        }

        amountInput = rootView.findViewById(R.id.amountField)
        amountInput.setCurrency(CurrencySymbols.USA)
        amountInput.setSpacing(true)
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = s.toString().trim { it <= ' ' }

                if (trimmed.isEmpty()) {
                    toggleCategoryButtons(false)
                } else {
                    if (!categoryButtons[0]!!.isEnabled) {
                        toggleCategoryButtons(true)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        dateInput = rootView.findViewById(R.id.dateField)
        dateInput.setText(today)

        dateSelector = rootView.findViewById(R.id.dateSelector)
        dateSelector.maxDate = sdf.parse(today)!!.time

        dateOverlay = rootView.findViewById(R.id.dateOverlay)

        toggleDateSelector(false)

        dateSelector.setOnDateChangeListener {
                view: CalendarView, year: Int, month: Int, day: Int ->
            toggleDateSelector(false)

            val monthOneBased = month + 1
            dateInput.setText(sdf.format(sdf.parse("$year-$monthOneBased-$day")!!))
        }

        dateButton = rootView.findViewById(R.id.dateButton)
        dateButton.setOnClickListener { toggleDateSelector(true) })

        return rootView
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