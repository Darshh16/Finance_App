package com.example.personalfinanceapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.personalfinanceapp.databinding.ActivityMainBinding
import com.example.personalfinanceapp.ui.dashboard.DashboardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load dashboard as the first screen
        loadFragment(DashboardFragment())

        // Handle bottom navigation taps
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_transactions -> { loadFragment(DashboardFragment()); true } // placeholder
                R.id.navigation_budget -> { loadFragment(DashboardFragment()); true }      // placeholder
                R.id.navigation_goals -> { loadFragment(DashboardFragment()); true }       // placeholder
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}
