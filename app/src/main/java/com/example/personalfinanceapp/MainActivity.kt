package com.example.personalfinanceapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.personalfinanceapp.databinding.ActivityMainBinding
import com.example.personalfinanceapp.ui.dashboard.DashboardFragment
import com.example.personalfinanceapp.ui.transaction.AddTransactionActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(DashboardFragment())

        // FAB opens Add Transaction screen
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_transactions -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_budget -> { loadFragment(DashboardFragment()); true }
                R.id.navigation_goals -> { loadFragment(DashboardFragment()); true }
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
