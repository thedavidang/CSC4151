package com.spendsages.walletwatch

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/* This class provides support for creating and switching between the three tabs. */
class SettingsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    /* Purpose: Getter/Accessor that opens the corresponding fragment
    * based on the index of the target tab to now display.
    *
    * Parameters: position represents the integer of the index of the target tab to now display.
    *
    * Returns: Nothing. */
    override fun getItem(position: Int): Fragment {
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
    override fun getCount(): Int {
        return 3
    }

    /* Purpose: Getter/Accessor that displays the title of each tab in the tab menubar settingsTabs.
    *
    * Parameters: position represents the integer of the index of the target tab to now display.
    *
    * Returns: Title of a tab. */
    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> "Terms"
            2 -> "About"
            else -> "Categories"
        }
    }
}