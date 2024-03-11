package com.spendsages.walletwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spendsages.walletwatch.databinding.ActivityMainBinding

/* This is the "main" of the program and is also the primary activity of the app.
* This will immediately load upon app launch. */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout

    lateinit var model : SharedViewModel

    override fun onDestroy() {
        super.onDestroy()
        /* Hide the keyboard. */
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()
        /* Hide the keyboard. */
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
    }

    override fun onPause() {
        super.onPause()
        /* Hide the keyboard. */
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
    }

    /* Overwritten function that performs tasks immediately upon app launch. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        /* Display the activity_main.xml layout. */
        setContentView(binding.root)

        /* Setup the tab layout mediator, which will load the three tabs and select Tab 1. */
        tabLayout = findViewById(R.id.mainTabs)
        binding.mainPager.adapter = MainPagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, binding.mainPager) { tab, position ->
            when (position) {
                1 -> {
                    /* Set tab title. */
                    tab.text = "Analytics"
                    /* Hide the numpad. */
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
                }
                2 -> {
                    /* Set tab title. */
                    tab.text = "History"
                    /* Hide the numpad. */
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
                }
                else -> {
                    /* Set tab title. */
                    tab.text = "Add"
                    /* Open the numpad. */
                    showKeyboard(
                        findViewById<com.cottacush.android.currencyedittext.CurrencyEditText>(
                            R.id.amountField
                        )
                    )
                }
            }
        }.attach()

        /* Function that will open the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProvider(this, viewModelFactory)[SharedViewModel::class.java]
        model.open(this)
    }

    /* Purpose: Force the focus on the given UI object and
    * then force the keyboard with the correct key layout to open.
    *
    * Parameters: view represents the UI object to set the focus on to.
    *
    * Returns: Nothing. */
    fun showKeyboard(view : View) {
        /* Set the focus on the UI object. */
        if (view.requestFocus()) {
            /* Open the keyboard that has the correct layout for the given UI textbox.
            * For example, if it is a numerical textbox, such as amountField,
            * it will open the numpad. */
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}