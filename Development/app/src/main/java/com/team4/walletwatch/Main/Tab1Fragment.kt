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
    private val today = LocalDate.now().toString()

    private lateinit var dateButton : ImageButton

    private val categoryButtons = Array<Button?>(3) { null }

    private lateinit var success : Toast

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

    private fun toggleCategoryButtons(isEnabled : Boolean) {
        for (button in categoryButtons) {
            if (isEnabled) {
                button?.isEnabled = true
                button?.isClickable = true
                button?.alpha = 1.0F
            }
            else {
                button?.isEnabled = false
                button?.isClickable = false
                button?.alpha = 0.5F
            }
        }
    }

    private fun submitEntry(category : Int) {
        var amount = amountInput.text.toString()
        var description = descriptionInput.text.toString()
        var date = dateInput.text.toString()

        if (amount.length > 14) {
            amount = "$ 9,999,999.99"
        }

        if (description.length > 50) {
            description = description.substring(0, 50)
        }

        DataManager.addEntry(model.get(), amount, description, date, category.toString())

        model.save(main)

        success.show()

        amountInput.setText("")
        descriptionInput.setText("")
        dateInput.setText(today)
        toggleCategoryButtons(false)
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
        dateButton.setOnClickListener { toggleDateSelector(true) }

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