package com.example.nehsus.togethar

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

/**
 * Created by Nehsus on 09/06/18.
 */
abstract class BaseActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null

    @JvmOverloads
    fun initToolbar(title: String, homeButtonEnabled: Boolean = false) {
        toolbar = findViewById(R.id.toolbar)
        if (toolbar == null)
            return

        toolbar!!.title = title
        setSupportActionBar(toolbar)

        if (supportActionBar == null)
            return

        supportActionBar!!.setDisplayHomeAsUpEnabled(homeButtonEnabled)
        supportActionBar!!.setHomeButtonEnabled(homeButtonEnabled)
    }
}
