package com.example.ping.util

data class User(
    val email: String? = "",
    val username: String? = "",
    val imageUrl: String? = "",
    val phone: String? = "",
    val followHashtags: ArrayList<String>? = arrayListOf(),
    val followUsers: ArrayList<String>? = arrayListOf()
)

data class Message(
    val messageId: String? = "",
    val userIds: ArrayList<String>? = arrayListOf(),
    val username: String? = "",
    val text: String? = "",
    val imageUrl: String? = "",
    val timestamp: Long? = 0,
    val hashtags: ArrayList<String>? = arrayListOf(),
    val likes: ArrayList<String>? = arrayListOf()
)

data class Contact(
    val username: String?,
    val phone: String?
)

data class Chat(
    val chatParticipants: ArrayList<String>
)

data class Convo(
    val sentBy: String? = "",
    val message: String? = "",
    val messageTime: Long? = 0
)
