package com.example.ping.listenerc

interface ChatClicklListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatImageUrl: String?, chatName: String?)
}