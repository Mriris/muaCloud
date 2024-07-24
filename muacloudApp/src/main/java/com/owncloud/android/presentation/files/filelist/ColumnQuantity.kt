

package com.owncloud.android.presentation.files.filelist

import android.content.Context
import android.util.DisplayMetrics
import android.view.View


class ColumnQuantity(context: Context, viewId: Int) {

    private var width: Int = 0
    private var height: Int = 0
    private var remaining: Int = 0
    private var displayMetrics: DisplayMetrics

    init {
        val view: View = View.inflate(context, viewId, null)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = view.measuredWidth
        height = view.measuredHeight
        displayMetrics = context.resources.displayMetrics
    }

    fun calculateNoOfColumns(): Int {
        var numberOfColumns = displayMetrics.widthPixels.div(width)
        remaining = displayMetrics.widthPixels.minus(numberOfColumns.times(width))
        if (remaining.div(numberOfColumns.times(2)) < 15) {
            numberOfColumns.minus(1)
            remaining = displayMetrics.widthPixels.minus(numberOfColumns.times(width))
        }
        return numberOfColumns
    }

}
