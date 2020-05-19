package com.spendsages.walletwatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
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

        settingsTabs.setupWithViewPager(settingsPager)

        /* Function that will close the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.closeSettingsButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
        model.open(this)
    }
}