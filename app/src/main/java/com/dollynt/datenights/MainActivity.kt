package com.dollynt.datenights

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dollynt.datenights.adapter.ViewPagerAdapter
import com.dollynt.datenights.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflando o layout e configurando a UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura o ViewPager e Navigation
        setupViewPagerAndNavigation()

        // Carrega o fragmento inicial
        if (savedInstanceState == null) {
            binding.viewPager.currentItem = 0
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }

    private fun setupViewPagerAndNavigation() {
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.navigation_couple -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.navigation_history -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.navigation_profile -> {
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    1 -> binding.bottomNavigation.selectedItemId = R.id.navigation_couple
                    2 -> binding.bottomNavigation.selectedItemId = R.id.navigation_history
                    3 -> binding.bottomNavigation.selectedItemId = R.id.navigation_profile
                }
            }
        })
    }
}
