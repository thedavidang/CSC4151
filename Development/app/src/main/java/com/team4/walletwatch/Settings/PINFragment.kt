package com.team4.walletwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PINFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PINFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pin, container, false)
        val settings = activity as SettingsActivity
        val model = settings.model

        val protectSwitch : Switch = rootView.findViewById(R.id.pinSwitch)

        val createText : TextView = rootView.findViewById(R.id.createLabel)
        val createPIN : EditText = rootView.findViewById(R.id.createEdit)

        val confirmText : TextView = rootView.findViewById(R.id.confirmLabel)
        val confirmPIN : EditText = rootView.findViewById(R.id.confirmEdit)

        val forgotButton : Button = rootView.findViewById(R.id.forgotPINButton)

        if (DataManager.getValueByID(model.get(), "p")!!.length == 4) {
            protectSwitch.isChecked = true

            createText.text = resources.getString(R.string.changePINString)
            createText.alpha = 1.0F

            createPIN.isEnabled = true
            createPIN.isClickable = true
            createPIN.alpha = 1.0F

            confirmText.text = resources.getString(R.string.confirmNewPINString)
            confirmText.alpha = 0.5F

            confirmPIN.isEnabled = false
            confirmPIN.isClickable = false
            confirmPIN.alpha = 0.5F

            forgotButton.isEnabled = true
            forgotButton.isClickable = true
            forgotButton.alpha = 1.0F
        }
        else {
            createText.alpha = 0.5F

            createPIN.isEnabled = false
            createPIN.isClickable = false
            createPIN.alpha = 0.5F

            confirmText.alpha = 0.5F

            confirmPIN.isEnabled = false
            confirmPIN.isClickable = false
            confirmPIN.alpha = 0.5F

            forgotButton.isEnabled = false
            forgotButton.isClickable = false
            forgotButton.alpha = 0.5F
        }

        protectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                createText.text = resources.getString(R.string.createPINString)
                createText.alpha = 1.0F

                createPIN.isEnabled = true
                createPIN.isClickable = true
                createPIN.alpha = 1.0F

                confirmText.text = resources.getString(R.string.confirmPINString)
                confirmText.alpha = 0.5F

                confirmPIN.isEnabled = false
                confirmPIN.isClickable = false
                confirmPIN.alpha = 0.5F

                forgotButton.isEnabled = true
                forgotButton.isClickable = true
                forgotButton.alpha = 1.0F
            } else {
                createText.text = resources.getString(R.string.createPINString)
                createText.alpha = 0.5F

                createPIN.isEnabled = false
                createPIN.isClickable = false
                createPIN.alpha = 0.5F

                confirmText.text = resources.getString(R.string.confirmPINString)
                confirmText.alpha = 0.5F

                confirmPIN.isEnabled = false
                confirmPIN.isClickable = false
                confirmPIN.alpha = 0.5F

                forgotButton.isEnabled = false
                forgotButton.isClickable = false
                forgotButton.alpha = 0.5F
            }
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
         * @return A new instance of fragment PINFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PINFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
