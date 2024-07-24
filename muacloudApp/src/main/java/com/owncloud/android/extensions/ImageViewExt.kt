

package com.owncloud.android.extensions

import android.annotation.SuppressLint
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

@SuppressLint("CheckResult")
fun ImageView.setPicture(imageToLoad: Int) {
    Glide.with(this)
        .load(imageToLoad)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
}
