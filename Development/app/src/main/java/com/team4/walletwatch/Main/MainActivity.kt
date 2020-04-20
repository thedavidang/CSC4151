package com.team4.walletwatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

/* This is the "main" of the program and is also the primary activity of the app.
* This will immediately load upon app launch. */
class MainActivity : AppCompatActivity() {
    lateinit var model : SharedViewModel

    /* Overwritten function that performs tasks immediately upon app launch */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Display the activity_main.xml layout. */
        setContentView(R.layout.activity_main)

        /* Setup the fragment manager, which will load the three tabs and select Tab 1. */
        val fragmentAdapter = MainPagerAdapter(supportFragmentManager)
        mainPager.adapter = fragmentAdapter

        mainTabs.setupWithViewPager(mainPager)

        /* Function that will open the Settings activity when the user taps the Settings button. */
        findViewById<ImageButton>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        /* Setup the shared view model, so that all fragments can access the same live data. */
        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
        model.open(this)
    }
}