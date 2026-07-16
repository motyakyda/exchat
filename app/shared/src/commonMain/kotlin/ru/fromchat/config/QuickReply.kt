package ru.fromchat.config

import kotlinx.serialization.Serializable

@Serializable
data class QuickReply(
    val id: String,
    val shortcut: String,
    val message: String
)
