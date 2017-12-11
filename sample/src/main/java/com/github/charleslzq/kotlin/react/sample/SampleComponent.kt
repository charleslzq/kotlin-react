package com.github.charleslzq.kotlin.react.sample

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus

/**
 * Created by charleslzq on 17-12-11.
 */
class SampleComponent(
        baseView: View,
        sampleStore: SampleStore
) : Component<View, SampleStore>(baseView, sampleStore) {
    private val button: Button = view.findViewById(R.id.button)
    private val text: TextView = view.findViewById(R.id.text)

    init {
        button.setOnClickListener {
            EventBus.post(ClickEvent())
        }

        render(SampleStore::count) {
            text.post { text.text = "$it" }
        }
    }
}