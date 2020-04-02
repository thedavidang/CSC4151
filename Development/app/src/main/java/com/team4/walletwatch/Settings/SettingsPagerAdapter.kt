package com.team4.walletwatch

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class SettingsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> PINFragment()
            2 -> AboutFragment()
            else -> CategoryFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> "PIN"
            2 -> "About"
            else -> "Categories"
        }
    }
}