package com.github.charleslzq.kotlin.react.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.charleslzq.kotlin.react.sample.R.layout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var sampleComponent: SampleComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        sampleComponent = SampleComponent(sampleMain, SampleStore())
    }
}
