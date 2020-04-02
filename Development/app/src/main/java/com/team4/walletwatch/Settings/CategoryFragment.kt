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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_category, container, false)
        val settings = activity as SettingsActivity
        val model = settings.model
        val categories = DataManager.getCategories(model.get())
        val changed = ArrayList<String?>(3)
        changed.add(null)
        changed.add(null)
        changed.add(null)

        val saveButton : Button = rootView.findViewById(R.id.saveButton)
        saveButton.isEnabled = false
        saveButton.isClickable = false
        saveButton.alpha = 0.5F
        saveButton.setOnClickListener {
            DataManager.overwriteCategories(model.get(), changed)

            model.save(settings)

            saveButton.isEnabled = false
            saveButton.isClickable = false
            saveButton.alpha = 0.5F
        }

        val category1Edit : EditText = rootView.findViewById(R.id.category1Edit)
        category1Edit.setText(categories[1])
        category1Edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = s.toString().trim { it <= ' ' }

                if (trimmed.isEmpty()) {
                    saveButton.isEnabled = false
                    saveButton.isClickable = false
                    saveButton.alpha = 0.5F
                } else {
                    if (!saveButton.isEnabled) {
                        saveButton.isEnabled = true
                        saveButton.isClickable = true
                        saveButton.alpha = 1.0F
                    }
                    changed[0] = category1Edit.text.toString()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        val category2Edit : EditText = rootView.findViewById(R.id.category2Edit)
        category2Edit.setText(categories[2])
        category2Edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = s.toString().trim { it <= ' ' }

                if (trimmed.isEmpty()) {
                    saveButton.isEnabled = false
                    saveButton.isClickable = false
                    saveButton.alpha = 0.5F
                } else {
                    if (!saveButton.isEnabled) {
                        saveButton.isEnabled = true
                        saveButton.isClickable = true
                        saveButton.alpha = 1.0F
                    }
                    changed[0] = category2Edit.text.toString()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        val category3Edit : EditText = rootView.findViewById(R.id.category3Edit)
        category3Edit.setText(categories[3])
        category3Edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val trimmed = s.toString().trim { it <= ' ' }

                if (trimmed.isEmpty()) {
                    saveButton.isEnabled = false
                    saveButton.isClickable = false
                    saveButton.alpha = 0.5F
                } else {
                    if (!saveButton.isEnabled) {
                        saveButton.isEnabled = true
                        saveButton.isClickable = true
                        saveButton.alpha = 1.0F
                    }
                    changed[0] = category3Edit.text.toString()
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

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
