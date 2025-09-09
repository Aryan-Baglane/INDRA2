package com.example.indra.data

// Or your relevant package

import java.util.UUID

enum class Sender {
    USER, BOT
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)