

package com.owncloud.android.presentation.security.passcode

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.max
import kotlin.math.min


class SquareFrameLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthSize == 0 && heightSize == 0) {

            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            val minSize = min(measuredWidth, measuredHeight)
            setMeasuredDimension(minSize, minSize)
            return
        }

        val size = if (widthSize == 0 || heightSize == 0) {


            max(widthSize, heightSize)
        } else {


            min(widthSize, heightSize)
        }

        val newMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        super.onMeasure(newMeasureSpec, newMeasureSpec)
    }
}
