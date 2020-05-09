package com.team4.walletwatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    private lateinit var recycler : RecyclerView
    private lateinit var adapterRecycler : RecyclerAdapter

    private lateinit var deleteButton : Button

    private lateinit var spinSorting : Spinner

    private lateinit var spinFiltering : Spinner

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_tab3, container, false)
        main = activity as MainActivity
        model = main.model

        /* Setup the RecyclerView, which will dynamically load entry cards as the user scolls. */
        recycler = rootView.findViewById(R.id.cardRecycler)
        /* Set the RecyclerView to have a vertical layout. */
        val layout = LinearLayoutManager(context)
        recycler.layoutManager = layout
        /* Connect the RecyclerView to the data model using the RecyclerAdapter. */
        adapterRecycler = RecyclerAdapter(model.get())
        recycler.adapter = adapterRecycler

        /* Initially set the "Delete Selected" button to be disabled and greyed-out. */
        deleteButton = rootView.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.isClickable = false
        deleteButton.alpha = 0.5F
        /* TODO (SPEN-8): Display Confirmation alert that confirms that the user would like
        *   to delete X entries and that it cannot be undone. */

        /* TODO (SPEN-32): If the user taps "Yes", then call the back-end function that
        *   deletes a given list of selected entries.
        *   Then disable the "Delete Selected" button.
        *   Save the changes using: model.save(main)
        *   The deleted entries should now no longer appear in Tab 3 whatsoever.
        *   If the user taps "No", then simply close the Confirmation alert. */



        /* TODO (SPEN-8): Display Edit window for user to modify existing entry. */

        /* TODO (SPEN-8): Display Confirmation alert that confirms that the user would like
        *   to edit fields X, Y, ... of the entry. */

        /* TODO (SPEN-33): If the user taps "Yes", then call the back-end function that
        *   edits a given entry.
        *   Then, close the Edit Entry window.
        *   Save the changes using: model.save(main)
        *   The modified entry should now reflect the changes in Tab 3.
        *   If the user taps "No", then simply close the Confirmation alert,
        *   but do not close the Edit Entry window. */

        /* Set the possible options for the sorting Spinbox. */
        spinSorting = rootView.findViewById(R.id.sortingSpinner)
        val adapterSorting = ArrayAdapter(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.sortingOptions))
        spinSorting.adapter = adapterSorting
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
        val adapterFiltering = ArrayAdapter<String?>(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            DataManager.getCategories(model.get()))
        spinFiltering.adapter = adapterFiltering
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
