package com.dollynt.datenights

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dollynt.datenights.adapter.ViewPagerAdapter
import com.dollynt.datenights.databinding.ActivityMainBinding
import com.dollynt.datenights.ui.couple.CoupleViewModel
import com.dollynt.datenights.ui.login.LoginActivity
import com.dollynt.datenights.ui.user.UserViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redireciona para LoginActivity se o usuário não estiver autenticado
        if (Firebase.auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]
        viewPagerAdapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = viewPagerAdapter

        coupleViewModel.checkCoupleStatus()

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

        // Carrega o fragmento inicial
        if (savedInstanceState == null) {
            binding.viewPager.currentItem = 0
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }
}
