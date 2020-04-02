package com.team4.walletwatch

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> Tab2Fragment()
            2 -> Tab3Fragment()
            else -> Tab1Fragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> "Analytics"
            2 -> "History"
            else -> "Add"
        }
    }
}