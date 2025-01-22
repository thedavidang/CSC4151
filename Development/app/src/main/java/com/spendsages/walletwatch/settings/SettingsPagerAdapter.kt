package com.spendsages.walletwatch.settings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/* This class provides support for creating and switching between the three tabs. */
class SettingsPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    /* Purpose: Getter/Accessor that opens the corresponding fragment
    * based on the index of the target tab to now display.
    *
    * Parameters: position represents the integer of the index of the target tab to now display.
    *
    * Returns: Nothing. */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> TermsFragment()
            2 -> AboutFragment()
            else -> CategoryFragment()
        }
    }

    /* Purpose: Getter/Accessor that returns how many tabs there are.
    *
    * Parameters: None.
    *
    * Returns: 3 since there are only ever the three tabs "Categories", "Terms", and "About". */
    override fun getItemCount(): Int {
        return 3
    }
}