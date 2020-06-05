package com.spendsages.walletwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab1.*

/* This is the "main" of the program and is also the primary activity of the app.
* This will immediately load upon app launch. */
class MainActivity : AppCompatActivity() {
    lateinit var model : SharedViewModel

    override fun onDestroy() {
        super.onDestroy()
        /* Hide the keyboard. */
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(mainPager.windowToken, 0)
    }

    /* Overwritten function that performs tasks immediately upon app launch. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Display the activity_main.xml layout. */
        setContentView(R.layout.activity_main)

        /* Setup the fragment manager, which will load the three tabs and select Tab 1. */
        val fragmentAdapter = MainPagerAdapter(supportFragmentManager)
        mainPager.adapter = fragmentAdapter
        mainPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            /* Listener that will force the numpad to open on Tab 1
            * and force the numpad to close when not on Tab 1. */
            override fun onPageScrollStateChanged(state: Int) {
                /* Wait until the Tab scrolling animation is done. */
                if (state == ViewPager.SCROLL_STATE_IDLE)
                {
                    /* Check if the current tab is not Tab 1. */
                    if (mainPager.currentItem != 0)
                    {
                        /* Hide the keyboard. */
                        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(mainPager.windowToken, 0)
                    }
                    /* Otherwise, open the numpad. */
                    else {
                        showKeyboard(amountField)
                    }
                }
            }
        })

        mainTabs.setupWithViewPager(mainPager)

        /* Function that will open the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
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
                .toggleSoftInput(
                    InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }
}