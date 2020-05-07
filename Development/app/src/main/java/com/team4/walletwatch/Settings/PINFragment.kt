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
    private lateinit var rootView : View
    private lateinit var settings : SettingsActivity
    private lateinit var model : SharedViewModel

    private lateinit var protectSwitch : Switch

    private lateinit var createText : TextView
    private lateinit var createPIN : EditText

    private lateinit var confirmText : TextView
    private lateinit var confirmPIN : EditText

    private lateinit var forgotButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_pin, container, false)
        settings = activity as SettingsActivity
        model = settings.model

        protectSwitch = rootView.findViewById(R.id.pinSwitch)

        createText = rootView.findViewById(R.id.createLabel)
        createPIN = rootView.findViewById(R.id.createEdit)

        confirmText = rootView.findViewById(R.id.confirmLabel)
        confirmPIN = rootView.findViewById(R.id.confirmEdit)

        forgotButton = rootView.findViewById(R.id.forgotPINButton)

        /* Check if there is an active PIN set in the XML file. */
        if (DataManager.getValueByID(model.get(), "p")!!.length == 4) {
            /* Denote PIN Protection as enabled. */
            protectSwitch.isChecked = true

            /* Set text of Create PIN label to "Change PIN:" since there is an active PIN.
            * Then, make it completely opaque. */
            createText.text = resources.getString(R.string.changePINString)
            createText.alpha = 1.0F

            /* Enable the Create Pin Textbox, which will be used to change the PIN
            * since there is an active PIN. */
            createPIN.isEnabled = true
            createPIN.isClickable = true
            createPIN.alpha = 1.0F

            /* Set text of Create PIN label to "Confirm New PIN:" since there is an active PIN.
            * Then, make it greyed out since the user
            * has not entered anything in the Create/Change PIN textbox. */
            confirmText.text = resources.getString(R.string.confirmNewPINString)
            confirmText.alpha = 0.5F

            /* Disable and grey-out the Confirm Pin Textbox, since the user has not yet entered
            * anything in the Create/Change PIN Textbox.
            * This Textbox will be used to confirm the changed PIN since there is an active PIN. */
            confirmPIN.isEnabled = false
            confirmPIN.isClickable = false
            confirmPIN.alpha = 0.5F

            /* Enable the Forgot PIN button since there is an active PIN. */
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
