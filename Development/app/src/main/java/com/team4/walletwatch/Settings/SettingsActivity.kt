package com.team4.walletwatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    lateinit var model : SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val fragmentAdapter = SettingsPagerAdapter(supportFragmentManager)
        settingsPager.adapter = fragmentAdapter

        settingsTabs.setupWithViewPager(settingsPager)

        findViewById<ImageButton>(R.id.closeSettingsButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
        model.open(this)
    }
}