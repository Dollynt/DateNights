package com.dollynt.datenights.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dollynt.datenights.ui.couple.CoupleFragment
import com.dollynt.datenights.ui.history.HistoryFragment
import com.dollynt.datenights.ui.home.HomeFragment
import com.dollynt.datenights.ui.profile.ProfileFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CoupleFragment()
            2 -> HistoryFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }

    override fun getItemCount(): Int = 4
}
