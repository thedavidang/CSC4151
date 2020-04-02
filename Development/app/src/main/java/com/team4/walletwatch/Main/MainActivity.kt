package com.team4.walletwatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var model : SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val fragmentAdapter = MainPagerAdapter(supportFragmentManager)
        mainPager.adapter = fragmentAdapter

        mainTabs.setupWithViewPager(mainPager)

        findViewById<ImageButton>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val viewModelFactory = Injection.provideViewModelFactory(this)
        model = ViewModelProviders.of(this, viewModelFactory).get(
            SharedViewModel::class.java)
        model.open(this)
    }
}