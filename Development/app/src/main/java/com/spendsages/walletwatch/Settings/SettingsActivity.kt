package com.spendsages.walletwatch

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.spendsages.walletwatch.databinding.ActivitySettingsBinding

/* This is the secondary activity of the app,
* which allows the user to view and modify various settings. */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    lateinit var model : SharedViewModel

    /* Overwritten function that performs tasks immediately upon opening Settings. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        /* Display the activity_settings.xml layout. */
        setContentView(binding.root)

        /* Setup the fragment manager, which will load the three tabs and select "Categories". */
        val fragmentAdapter = SettingsPagerAdapter(supportFragmentManager)
        binding.settingsPager.adapter = fragmentAdapter
        binding.settingsPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
                        .hideSoftInputFromWindow(binding.settingsPager.windowToken, 0)
                }
            }
        })

        binding.settingsTabs.setupWithViewPager(binding.settingsPager)

        /* Function that will close the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.closeSettingsButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProvider(this, viewModelFactory)[SharedViewModel::class.java]
        model.open(this)
    }

    override fun onBackPressed() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}