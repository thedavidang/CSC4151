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
   fun sortEntries(adapter : RecyclerAdapter, position : Int) {
        when (position) {
            1 -> adapter.entries = sortByDateAscending(adapter.entries)
            2 -> adapter.entries = sortByPriceDescending(adapter.entries)
            3 -> adapter.entries = sortByPriceAscending(adapter.entries)
            else -> adapter.entries = sortByDateDescending(adapter.entries)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tab3, container, false)
        val main = activity as MainActivity
        val model = main.model

        val recycler : RecyclerView = rootView.findViewById(R.id.cardRecycler)
        recycler.setHasFixedSize(true)
        val layout = LinearLayoutManager(context)
        recycler.layoutManager = layout
        val adapter = RecyclerAdapter(model.get())
        recycler.adapter = adapter

        val deleteButton: Button = rootView.findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.isClickable = false
        deleteButton.alpha = 0.5F

        val spinSorting : Spinner = rootView.findViewById(R.id.sortingSpinner)
        val adapterSorting = ArrayAdapter(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.sortingOptions))
        spinSorting.adapter = adapterSorting
        spinSorting.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                sortEntries(adapter, position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        val spinFiltering : Spinner = rootView.findViewById(R.id.filteringSpinner)
        val adapterFiltering = ArrayAdapter<String?>(requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            DataManager.getCategories(model.get()))
        spinFiltering.adapter = adapterFiltering
        spinFiltering.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                when (position) {
                    0 -> adapter.entries = adapter.entriesRaw
                    else -> adapter.filter(spinFiltering.selectedItem.toString())
                }
                sortEntries(adapter, spinSorting.selectedItemPosition)
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
