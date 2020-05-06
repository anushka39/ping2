package com.example.ping.listenerc

import com.example.ping.util.StatusListElement

interface StatusItemClickListener {
    fun onItemClicked(statusElement: StatusListElement)
}