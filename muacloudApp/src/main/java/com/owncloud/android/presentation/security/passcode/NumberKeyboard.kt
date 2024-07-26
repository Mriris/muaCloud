

package com.owncloud.android.presentation.security.passcode

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.owncloud.android.R


class NumberKeyboard(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private lateinit var numericKeys: MutableList<TextView>
    private lateinit var backspaceBtn: ImageView

    private var listener: NumberKeyboardListener? = null

    init {
        inflateView()
    }


    fun setListener(listener: NumberKeyboardListener?) {
        this.listener = listener
    }


    private fun inflateView() {
        val view = View.inflate(context, R.layout.numberkeyboard_layout, this)

        numericKeys = ArrayList(10)
        numericKeys.add(view.findViewById(R.id.key0))
        numericKeys.add(view.findViewById(R.id.key1))
        numericKeys.add(view.findViewById(R.id.key2))
        numericKeys.add(view.findViewById(R.id.key3))
        numericKeys.add(view.findViewById(R.id.key4))
        numericKeys.add(view.findViewById(R.id.key5))
        numericKeys.add(view.findViewById(R.id.key6))
        numericKeys.add(view.findViewById(R.id.key7))
        numericKeys.add(view.findViewById(R.id.key8))
        numericKeys.add(view.findViewById(R.id.key9))

        backspaceBtn = view.findViewById(R.id.backspaceBtn)

        setupListeners()
    }


    private fun setupListeners() {
        for (i in numericKeys.indices) {
            val key = numericKeys[i]
            key.setOnClickListener {
                listener?.onNumberClicked(i)
            }
        }

        backspaceBtn.setOnClickListener {
            listener?.onBackspaceButtonClicked()
        }
    }
}
