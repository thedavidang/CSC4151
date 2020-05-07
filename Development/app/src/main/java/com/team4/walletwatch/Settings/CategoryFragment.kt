package com.team4.walletwatch

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CategoryFragment : Fragment() {
    private lateinit var rootView : View
    private lateinit var settings : SettingsActivity
    private lateinit var model : SharedViewModel

    private lateinit var categories : MutableList<String?>

    /* Array that holds the new label for each category that the user changes.
    * If there is no change to a category, that index MUST be null. */
    private var changed = arrayOfNulls<String?>(3)

    private lateinit var saveButton : Button

    private val categoryTextboxes = Array<EditText?>(3) { null }

    /* Purpose: Controller method that disables and greys-out Save Changes button or
    * enables and reveals the Save Changes button.
    *
    * Parameters: enable represents a Boolean of whether or not
    *             to enable the Save Changes button.
    *
    * Returns: Nothing. */
    private fun toggleSaveButton(enable : Boolean) {
        /* Only enable if not already enabled. */
        if (enable && !saveButton.isEnabled) {
            saveButton.isEnabled = true
            saveButton.isClickable = true
            /* Set opacity to 100 % */
            saveButton.alpha = 1.0F
        }
        /* Only disable if not already disabled. */
        else if (!enable && saveButton.isEnabled) {
            saveButton.isEnabled = false
            saveButton.isClickable = false
            /* Set opacity to 50 % */
            saveButton.alpha = 0.5F
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_category, container, false)
        settings = activity as SettingsActivity
        model = settings.model

        /* Grab the labels of the categories as they currently are in the XML file. */
        categories = DataManager.getCategories(model.get())

        saveButton = rootView.findViewById(R.id.saveButton)
        /* Disable and grey-out the Save Changes button,
        * since the user has not made any changes yet. */
        toggleSaveButton(false)

        /* This listener for the Save Change button simply opens the Confirmation alert. */
        saveButton.setOnClickListener {
            /* TODO (SPEN-37): Simply open the Confirmation alert inside this listener,
            *   which likely will be the only line in this function. */

            /* TODO (SPEN-11): Move below code out of this listener and into the code for
            *   the Confirmation alert. This, of course, first requires creating the UI widget for
            *   the Confirmation alert first and writing the setup code for it. */
            DataManager.changeCategories(settings, model.get(), changed)
            model.save(settings)
            toggleSaveButton(false)
        }

        /* Populate the array of category textboxes. */
        categoryTextboxes[0] = rootView.findViewById(R.id.category1Edit)
        categoryTextboxes[1] = rootView.findViewById(R.id.category2Edit)
        categoryTextboxes[2] = rootView.findViewById(R.id.category3Edit)

        /* Iterate through each category textbox. */
        for ((index, textbox) in categoryTextboxes.withIndex()) {
            /* Set the label for each category textbox as they currently are in the XML file. */
            textbox!!.setText(categories[index + 1])

            /* Listener that checks if the user changed the category textbox content. */
            textbox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int) {}

                /* TODO (SPEN-37): Modify this listener such that the Save Changes button
                *   is enabled if and only if all three category textboxes actually have
                *   word(s) in them AND at least one of them has a category label that is
                *   not in the "categories" array.
                *   In addition, make sure to update the "changed" array accordingly,
                *   such that new category labels are placed in the correct position in the
                *   "changed" array, but the position of an existing category label is "null". */
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    /* Remove all whitespace from user input in category textbox. */
                    val trimmed = s.toString().trim { it <= ' ' }

                    /* Check if the user did not enter any word(s) into the category textbox. */
                    if (trimmed.isEmpty()) {
                        /* Disable and grey-out the Save Changes button. */
                        toggleSaveButton(false)

                        /* TODO (SPEN-37): Move/modify this line as necessary.
                        *   This simply exists currently to serve as a demonstrable way
                        *   to show that changing categories works, but you will
                        *   need to implement additional code that correctly sets up
                        *   the "changed" array as mentioned above.*/
                        changed[index] = null
                    }
                    else {
                        /* Enable and reveal the Save Changes button. */
                        toggleSaveButton(true)

                        /* TODO (SPEN-37): Move/modify this line as necessary.
                        *   This simply exists currently to serve as a demonstrable way
                        *   to show that changing categories works, but you will
                        *   need to implement additional code that correctly sets up
                        *   the "changed" array as mentioned above.*/
                        changed[index] = categoryTextboxes[index]!!.text.toString()
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            })
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
         * @return A new instance of fragment CategoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
