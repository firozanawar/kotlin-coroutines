package com.firozanwar.kotlincoroutines

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToBasicLevel1Activity(view: View) {
        startActivity(Intent(this, CodingWithMitchActivity::class.java))
    }
}
