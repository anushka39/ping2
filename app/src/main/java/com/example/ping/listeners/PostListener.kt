package com.example.ping.listeners

import com.example.ping.util.Message

interface PostListener {
    fun onLayoutClick(message: Message?)
    fun onLike(message: Message?)
    fun onRePost(message: Message?)

}