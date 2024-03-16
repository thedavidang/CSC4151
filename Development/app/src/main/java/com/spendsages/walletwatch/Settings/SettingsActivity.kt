package com.spendsages.walletwatch

import SharedViewModelFactory
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spendsages.walletwatch.databinding.ActivitySettingsBinding

/* This is the secondary activity of the app,
* which allows the user to view and modify various settings. */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var app: App

    lateinit var model: SharedViewModel

    override fun onDestroy() {
        super.onDestroy()
        this.viewModelStore.clear()
    }

    /* Overwritten function that performs tasks immediately upon opening Settings. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = (application as App)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        /* Display the activity_settings.xml layout. */
        setContentView(binding.root)

        /* Setup the tab layout mediator, which will load the three tabs and select Tab 1. */
        tabLayout = findViewById(R.id.settingsTabs)
        binding.settingsPager.adapter = SettingsPagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, binding.settingsPager) { tab, position ->
            when (position) {
                1 -> {
                    /* Set tab title. */
                    tab.text = "Terms"
                }
                2 -> {
                    /* Set tab title. */
                    tab.text = "About"
                }
                else -> {
                    /* Set tab title. */
                    tab.text = "Categories"
                }
            }

            /* Hide the keyboard. */
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.settingsPager.windowToken, 0)
        }.attach()

        /* Setup the shared view model, so that all fragments can access the same live data. */
        model = ViewModelProvider(app,
            SharedViewModelFactory(app.applicationContext))[SharedViewModel::class.java]

        /* Function that will close the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.closeSettingsButton).setOnClickListener {
            finish()
        }
    }
}