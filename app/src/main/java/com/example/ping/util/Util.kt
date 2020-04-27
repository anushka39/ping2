package com.example.ping.util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.ping.R
import java.text.DateFormat
import java.util.*


fun ImageView.loadUrl(url: String?, errorDrawable: Int= R.drawable.empty) {
   context?.let {
        val options = RequestOptions()
            .placeholder(progressDrawable(context))
            .error(errorDrawable)
        Glide.with(context.applicationContext)
            .load(url)
            .apply(options)
            .into(this)
    }
}

fun populateImage(context: Context?, uri: String?, imageView: ImageView, errorDrawable: Int = R.drawable.empty) {
    if(context != null) {
        val options = RequestOptions()
            .placeholder(progressDrawable(context))
            .error(errorDrawable)
        Glide.with(context)
            .load(uri)
            .apply(options)
            .into(imageView)
    }
}

fun progressDrawable(context: Context): CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }
}

fun getDate(s: Long?): String {
    s?.let {
        val df = DateFormat.getInstance()
        return df.format(Date(it))
    }
    return  "Unknown"
}