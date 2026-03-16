package com.example.personalfinanceapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.personalfinanceapp.databinding.ActivityMainBinding
import com.example.personalfinanceapp.ui.budget.BudgetFragment
import com.example.personalfinanceapp.ui.dashboard.DashboardFragment
import com.example.personalfinanceapp.ui.profile.ProfileFragment
import com.example.personalfinanceapp.ui.transaction.AddTransactionActivity
import com.example.personalfinanceapp.ui.transaction.TransactionsFragment
import com.example.personalfinanceapp.utils.SessionManager
import com.example.personalfinanceapp.ui.goals.GoalsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved dark mode preference BEFORE setContentView
        val sessionManager = SessionManager(this)
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(DashboardFragment())
        binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard

        // FAB — open Add Transaction screen
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    binding.fabAdd.show()
                    loadFragment(DashboardFragment()); true
                }
                R.id.navigation_transactions -> {
                    binding.fabAdd.show()
                    loadFragment(TransactionsFragment()); true
                }
                R.id.navigation_budget -> {
                    binding.fabAdd.hide()
                    loadFragment(BudgetFragment()); true
                }
                R.id.navigation_goals -> {
                    binding.fabAdd.hide()
                    loadFragment(GoalsFragment()); true
                }
                R.id.navigation_profile -> {
                    binding.fabAdd.hide()
                    loadFragment(ProfileFragment()); true
                }
                else -> false
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
