package com.dollynt.datenights

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.dollynt.datenights.adapter.ViewPagerAdapter
import com.dollynt.datenights.databinding.ActivityMainBinding
import com.dollynt.datenights.viewmodel.CoupleViewModel
import com.dollynt.datenights.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coupleViewModel: CoupleViewModel
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var inviteCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coupleViewModel = ViewModelProvider(this)[CoupleViewModel::class.java]

        // Inflando o layout, mas sem configurar a UI completa ainda
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Processa o Dynamic Link
        handleDynamicLink(intent) { inviteCodeExtracted ->
            // Se o usuário não estiver autenticado, redireciona para a LoginActivity
            if (Firebase.auth.currentUser == null) {
                navigateToLogin(inviteCodeExtracted)
            } else {
                // Se o usuário estiver autenticado, continue com a configuração da UI

                if (inviteCodeExtracted != null) {
                    coupleViewModel.joinCouple(Firebase.auth.currentUser!!.uid, inviteCodeExtracted)
                }

                coupleViewModel.checkCoupleStatus(Firebase.auth.uid)

                // Configura o ViewPager e Navigation
                setupViewPagerAndNavigation()

                // Carrega o fragmento inicial
                if (savedInstanceState == null) {
                    binding.viewPager.currentItem = 0
                }
            }
        }
    }

    private fun handleDynamicLink(intent: Intent, onLinkProcessed: (String?) -> Unit) {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                val extractedInviteCode = deepLink?.getQueryParameter("inviteCode")
                onLinkProcessed(extractedInviteCode)
            }
            .addOnFailureListener(this) {
                onLinkProcessed(null)
            }
    }

    private fun navigateToLogin(inviteCode: String?) {
        val loginIntent = Intent(this, LoginActivity::class.java).apply {
            putExtra("inviteCode", inviteCode)
        }
        startActivity(loginIntent)
        finish()
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

