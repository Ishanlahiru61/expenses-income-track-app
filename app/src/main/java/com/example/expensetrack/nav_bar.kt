package com.example.expensetrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class nav_bar : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var activeFragment: Fragment? = null
    private val fragmentManager = supportFragmentManager


    private val statsFragment = StatsFragment()
    private val addNewFragment = AddNewFragment()
    private val accountsFragment = AccountsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_bar)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupFragments()

        bottomNavigationView.selectedItemId = R.id.stats

        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment = when (item.itemId) {
                R.id.stats -> statsFragment
                R.id.add_new_item -> addNewFragment
                R.id.accounts -> accountsFragment
                else -> null
            }

            selectedFragment?.let { switchFragment(it) }
            true
        }
    }

    private fun setupFragments() {
        fragmentManager.beginTransaction()
            .add(R.id.frameLayout2, statsFragment, "STATS").show(statsFragment)
            .add(R.id.frameLayout2, addNewFragment, "ADD_NEW").hide(addNewFragment)
            .add(R.id.frameLayout2, accountsFragment, "ACCOUNTS").hide(accountsFragment)
            .commit()

        activeFragment = statsFragment
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            fragmentManager.beginTransaction()
                .hide(activeFragment!!)
                .show(fragment)
                .commit()


            fragment.onResume()

            activeFragment = fragment
        }
    }
}
