package com.spendsages.walletwatch.settings

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.databinding.FragmentTermsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [TermsFragment] constructor method to
 * create an instance of this fragment.
 */
class TermsFragment : Fragment() {
    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!

    private lateinit var termsText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        /* Allow Terms of Use text to be scrollable. */
        termsText = rootView.findViewById(R.id.termsText)
        termsText.movementMethod = ScrollingMovementMethod()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
