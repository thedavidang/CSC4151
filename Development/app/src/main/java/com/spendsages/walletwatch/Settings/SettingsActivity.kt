package com.spendsages.walletwatch

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_settings.*

/* This is the secondary activity of the app,
* which allows the user to view and modify various settings. */
class SettingsActivity : AppCompatActivity() {
    lateinit var model : SharedViewModel

    /* Overwritten function that performs tasks immediately upon opening Settings. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Display the activity_settings.xml layout. */
        setContentView(R.layout.activity_settings)

        /* Setup the fragment manager, which will load the three tabs and select "Categories". */
        val fragmentAdapter = SettingsPagerAdapter(supportFragmentManager)
        settingsPager.adapter = fragmentAdapter
        settingsPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            /* Listener that will force the numpad to close. */
            override fun onPageScrollStateChanged(state: Int) {
                /* Wait until the Tab scrolling animation is done. */
                if (state == ViewPager.SCROLL_STATE_IDLE)
                {
                    /* Hide the keyboard. */
                    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(settingsPager.windowToken, 0)
                }
            }
        })

        settingsTabs.setupWithViewPager(settingsPager)

        /* Function that will close the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.closeSettingsButton).setOnClickListener {
            finish()
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
        model.open(this)
    }
}