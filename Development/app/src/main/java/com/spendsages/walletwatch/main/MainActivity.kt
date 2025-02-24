package com.spendsages.walletwatch.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.spendsages.walletwatch.App
import com.spendsages.walletwatch.R
import com.spendsages.walletwatch.SharedViewModel
import com.spendsages.walletwatch.SharedViewModelFactory
import com.spendsages.walletwatch.databinding.ActivityMainBinding
import com.spendsages.walletwatch.settings.SettingsActivity
import java.text.DecimalFormat

/* This is the "main" of the program and is also the primary activity of the app.
* This will immediately load upon app launch. */
class MainActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tabLayout: TabLayout
    private lateinit var app: App

    lateinit var model: SharedViewModel
    lateinit var loadingBar: ProgressBar

    var appLaunched: Boolean = false
    var modelLoaded: Boolean = false

    /* Setup public constant for the metric prefixes corresponding to one thousand and above. */
    val metric = arrayOf("k", "M", "G", "T", "P", "E", "Z", "Y")

    override fun onDestroy() {
        super.onDestroy()
        /* Hide the keyboard. */
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
        app.viewModelStore.clear()
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
        app = (application as App)
        binding = ActivityMainBinding.inflate(layoutInflater)

        /* Display the activity_main.xml layout. */
        setContentView(binding.root)

        /* Setup the round progress bar circle that
        * will be displayed while a tab is loading. */
        loadingBar = findViewById(R.id.loadingBar)

        /* Setup the tab layout mediator, which will load the three tabs
        * and select Tab 1 at app launch. */
        tabLayout = findViewById(R.id.mainTabs)
        binding.mainPager.adapter = MainPagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(tabLayout, binding.mainPager) { tab, position ->
            /* Set tab title. */
            tab.text = when (position) {
                1 -> { getString(R.string.mainTab2String) }
                2 -> { getString(R.string.mainTab3String) }
                else -> { getString(R.string.mainTab1String) }
            }
        }.attach()

        binding.mainPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (!modelLoaded && state == SCROLL_STATE_DRAGGING) {
                    /* Show the loading bar while switching the tab, until the data model
                    * gets populated after Tab3Fragment is created. */
                    loadingBar.visibility = ProgressBar.VISIBLE
                } else if (state == SCROLL_STATE_IDLE) {
                    /* Hide the loading bar once tab is finished loading. */
                    loadingBar.visibility = ProgressBar.GONE
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (!appLaunched) {
                    showKeyboard(findViewById<EditText>(R.id.amountField))
                    appLaunched = true
                } else if (position == 2) {
                    /* Stop showing the loading bar when the data model
                    * is populated after Tab3Fragment is created. */
                    modelLoaded = true
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                /* Check if the app is past the launch stage and
                * that current tab is not Tab 1. */
                if (appLaunched && position != 0) {
                    /* Hide the keyboard. */
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(binding.mainPager.windowToken, 0)
                }
            }
        })

        /* Function that will open the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        model = ViewModelProvider(app,
            SharedViewModelFactory(app.applicationContext))[SharedViewModel::class.java]
    }

    /* Purpose: Force the focus on the given UI object and
    * then force the keyboard with the correct key layout to open.
    *
    * Parameters: view represents the UI object to set the focus on to.
    *
    * Returns: Nothing. */
    fun showKeyboard(view: View) {
        /* Set the focus on the UI object. */
        if (view.requestFocus()) {
            /* Open the keyboard that has the correct layout for the given UI textbox.
            * For example, if it is a numerical textbox, such as amountField,
            * it will open the numpad. */
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /* Purpose: Public helper method that formats a dollar amount to 2 decimals
    * with thousand-separator commas. If the amount is extremely large (i.e. one billion or more),
    * then display the number with metric prefixes instead.
    *
    * Parameters: amount represents the dollar amount to format.
    *
    * Returns: String of the formatted dollar amount. */
    fun formatDollarAmount(amount: Double): String {
        /* Compute base 10 exponent of dollar amount. */
        val exponent = kotlin.math.floor(kotlin.math.log10(amount))

        /* Check if dollar amount is less than one billion. */
        return if (exponent < 9) {
            /* Format dollar amount with dollar sign and thousand separator commas. */
            "$ " + DecimalFormat("#,##0.00").format(amount)
        }
        /* Otherwise, dollar amount is extremely large (one billion or more). */
        else {
            var totalString = amount.toLong().toString()
            /* Slice total to the first nine digits. */
            if (totalString.length > 9) {
                totalString = totalString.substring(0, 9)
            }
            /* Determine the position of where the first thousand separator would be. */
            val firstThousandSeparatorIndex = ((exponent % 3) + 1).toInt()
            /* Format extremely large dollar amount with dollar sign and metric prefix. */
            "$ " + totalString.substring(0, firstThousandSeparatorIndex) + "." +
                    totalString.substring(firstThousandSeparatorIndex) + " " +
                    metric[((exponent / 3) - 1).toInt()]
        }
    }
}